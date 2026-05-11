package com.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.ReportTemplate;
import java.util.Map;

public interface ReportService extends IService<ReportTemplate> {
    Map<String, Object> renderReport(String id, Map<String, Object> params);
    byte[] exportReport(String id, String format, Map<String, Object> params);
    ReportTemplate copyReport(String id);
    java.util.List<Map<String, Object>> getVersions(String id);
    void rollback(String id, String versionId);
}
