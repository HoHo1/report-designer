package com.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.Dataset;
import java.util.Map;

public interface DatasetService extends IService<Dataset> {
    Map<String, Object> testDataset(String id);
    java.util.List<Map<String, Object>> fetchData(String id, Map<String, Object> params);
    java.util.List<Map<String, Object>> parseFields(String id);
}
