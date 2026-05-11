package com.report.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.ReportTemplate;
import com.report.entity.ReportVersion;
import com.report.mapper.ReportTemplateMapper;
import com.report.mapper.ReportVersionMapper;
import com.report.service.ReportService;
import com.report.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements ReportService {

    @Autowired
    private ReportVersionMapper versionMapper;

    @Autowired
    private DatasetService datasetService;

    @Override
    public Map<String, Object> renderReport(String id, Map<String, Object> params) {
        ReportTemplate report = this.getById(id);
        if (report == null) {
            throw new RuntimeException("报表不存在");
        }

        JSONObject designContent = JSON.parseObject(report.getDesignContent());
        JSONObject datasetBindings = designContent.getJSONObject("datasetBindings");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> renderedData = new HashMap<>();

        if (datasetBindings != null) {
            for (String key : datasetBindings.keySet()) {
                JSONObject binding = datasetBindings.getJSONObject(key);
                String datasetId = binding.getString("datasetId");
                List<Map<String, Object>> data = datasetService.fetchData(datasetId, params);
                renderedData.put(key, data);
            }
        }

        result.put("designContent", designContent);
        result.put("renderedData", renderedData);
        result.put("reportId", id);
        result.put("reportName", report.getReportName());
        return result;
    }

    @Override
    public byte[] exportReport(String id, String format, Map<String, Object> params) {
        Map<String, Object> renderData = renderReport(id, params);
        return JSON.toJSONBytes(renderData);
    }

    @Override
    public ReportTemplate copyReport(String id) {
        ReportTemplate original = this.getById(id);
        if (original == null) {
            throw new RuntimeException("报表不存在");
        }

        ReportTemplate copy = new ReportTemplate();
        copy.setReportName(original.getReportName() + "_副本");
        copy.setDesignContent(original.getDesignContent());
        copy.setDatasetIds(original.getDatasetIds());
        copy.setCategory(original.getCategory());
        copy.setStatus(0);
        copy.setIsPublic(original.getIsPublic());
        copy.setCreateUser(original.getCreateUser());

        this.save(copy);
        return copy;
    }

    @Override
    public List<Map<String, Object>> getVersions(String id) {
        List<ReportVersion> versions = versionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReportVersion>()
                .eq(ReportVersion::getReportId, id)
                .orderByDesc(ReportVersion::getVersionNum)
                .last("LIMIT 10")
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportVersion v : versions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("versionNum", v.getVersionNum());
            map.put("createUser", v.getCreateUser());
            map.put("createTime", v.getCreateTime());
            result.add(map);
        }
        return result;
    }

    @Override
    public void rollback(String id, String versionId) {
        ReportVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }

        ReportTemplate report = this.getById(id);
        saveVersion(report);

        report.setDesignContent(version.getDesignContent());
        this.updateById(report);
    }

    public void saveVersion(ReportTemplate report) {
        Integer maxVersion = versionMapper.selectMaxVersionByReportId(report.getId());
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        ReportVersion version = new ReportVersion();
        version.setReportId(report.getId());
        version.setDesignContent(report.getDesignContent());
        version.setVersionNum(newVersion);
        version.setCreateUser(report.getCreateUser());

        versionMapper.insert(version);

        versionMapper.deleteOldVersions(report.getId(), 10);
    }
}
