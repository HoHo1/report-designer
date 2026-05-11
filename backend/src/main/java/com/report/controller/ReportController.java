package com.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.report.common.Result;
import com.report.entity.ReportTemplate;
import com.report.service.ReportService;
import com.report.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@Tag(name = "报表管理")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportServiceImpl reportServiceImpl;

    @PostMapping
    @Operation(summary = "创建报表")
    public Result<ReportTemplate> create(@RequestBody ReportTemplate report) {
        reportService.save(report);
        return Result.success(report);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报表")
    public Result<Void> update(@PathVariable String id, @RequestBody ReportTemplate report) {
        report.setId(id);
        reportServiceImpl.saveVersion(report);
        reportService.updateById(report);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报表")
    public Result<Void> delete(@PathVariable String id) {
        reportService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报表详情")
    public Result<ReportTemplate> getById(@PathVariable String id) {
        return Result.success(reportService.getById(id));
    }

    @GetMapping
    @Operation(summary = "获取报表列表")
    public Result<Page<ReportTemplate>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<ReportTemplate> pageData = new Page<>(page, size);
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();

        if (category != null && !category.isEmpty()) {
            wrapper.eq(ReportTemplate::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ReportTemplate::getReportName, keyword);
        }

        return Result.success(reportService.page(pageData, wrapper));
    }

    @PostMapping("/{id}/render")
    @Operation(summary = "渲染报表数据")
    public Result<Map<String, Object>> render(@PathVariable String id, @RequestBody Map<String, Object> params) {
        return Result.success(reportService.renderReport(id, params));
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出报表")
    public Result<byte[]> export(
            @PathVariable String id,
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam Map<String, Object> params) {
        return Result.success(reportService.exportReport(id, format, params));
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制报表")
    public Result<ReportTemplate> copy(@PathVariable String id) {
        return Result.success(reportService.copyReport(id));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "获取报表历史版本")
    public Result<List<Map<String, Object>>> getVersions(@PathVariable String id) {
        return Result.success(reportService.getVersions(id));
    }

    @PostMapping("/{id}/rollback/{versionId}")
    @Operation(summary = "回滚报表版本")
    public Result<Void> rollback(@PathVariable String id, @PathVariable String versionId) {
        reportService.rollback(id, versionId);
        return Result.success();
    }
}
