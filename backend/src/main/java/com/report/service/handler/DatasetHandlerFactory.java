package com.report.service.handler;

import org.springframework.stereotype.Component;

@Component
public class DatasetHandlerFactory {

    public DatasetHandler getHandler(String type) {
        return switch (type.toUpperCase()) {
            case "SQL" -> new SqlDatasetHandler();
            case "API" -> new ApiDatasetHandler();
            case "FILE" -> new FileDatasetHandler();
            default -> throw new IllegalArgumentException("不支持的数据集类型: " + type);
        };
    }
}
