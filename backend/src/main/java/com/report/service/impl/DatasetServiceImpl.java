package com.report.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.Dataset;
import com.report.mapper.DatasetMapper;
import com.report.service.DatasetService;
import com.report.service.handler.DatasetHandlerFactory;
import com.report.service.handler.DatasetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    @Autowired
    private DatasetHandlerFactory handlerFactory;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "dataset:";

    @Override
    public Map<String, Object> testDataset(String id) {
        Dataset dataset = this.getById(id);
        if (dataset == null) {
            throw new RuntimeException("数据集不存在");
        }

        DatasetHandler handler = handlerFactory.getHandler(dataset.getDatasetType());
        List<Map<String, Object>> data = handler.fetchData(dataset, new HashMap<>());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data.size() > 100 ? data.subList(0, 100) : data);
        result.put("total", data.size());
        return result;
    }

    @Override
    public List<Map<String, Object>> fetchData(String id, Map<String, Object> params) {
        String cacheKey = CACHE_PREFIX + id + ":" + params.hashCode();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Dataset dataset = this.getById(id);
        if (dataset == null) {
            throw new RuntimeException("数据集不存在");
        }

        DatasetHandler handler = handlerFactory.getHandler(dataset.getDatasetType());
        List<Map<String, Object>> data = handler.fetchData(dataset, params);

        redisTemplate.opsForValue().set(cacheKey, data, 5, TimeUnit.MINUTES);
        return data;
    }

    @Override
    public List<Map<String, Object>> parseFields(String id) {
        Dataset dataset = this.getById(id);
        if (dataset == null) {
            throw new RuntimeException("数据集不存在");
        }

        DatasetHandler handler = handlerFactory.getHandler(dataset.getDatasetType());
        return handler.parseFields(dataset);
    }

    public void clearCache(String id) {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + id + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
