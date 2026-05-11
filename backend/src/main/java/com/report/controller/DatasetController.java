package com.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.report.common.Result;
import com.report.entity.Dataset;
import com.report.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dataset")
@Tag(name = "数据集管理")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @PostMapping
    @Operation(summary = "创建数据集")
    public Result<Dataset> create(@RequestBody Dataset dataset) {
        datasetService.save(dataset);
        return Result.success(dataset);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据集")
    public Result<Void> update(@PathVariable String id, @RequestBody Dataset dataset) {
        dataset.setId(id);
        datasetService.updateById(dataset);
        datasetService.clearCache(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据集")
    public Result<Void> delete(@PathVariable String id) {
        datasetService.removeById(id);
        datasetService.clearCache(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据集详情")
    public Result<Dataset> getById(@PathVariable String id) {
        return Result.success(datasetService.getById(id));
    }

    @GetMapping
    @Operation(summary = "获取数据集列表")
    public Result<Page<Dataset>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Page<Dataset> pageData = new Page<>(page, size);
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();

        if (type != null && !type.isEmpty()) {
            wrapper.eq(Dataset::getDatasetType, type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Dataset::getDatasetName, keyword);
        }

        return Result.success(datasetService.page(pageData, wrapper));
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试数据集")
    public Result<Map<String, Object>> test(@PathVariable String id) {
        return Result.success(datasetService.testDataset(id));
    }

    @GetMapping("/{id}/fields")
    @Operation(summary = "获取数据集字段")
    public Result<List<Map<String, Object>>> getFields(@PathVariable String id) {
        return Result.success(datasetService.parseFields(id));
    }
}
