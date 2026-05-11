package com.report.service.handler;

import com.report.entity.Dataset;
import java.util.List;
import java.util.Map;

public interface DatasetHandler {
    List<Map<String, Object>> fetchData(Dataset dataset, Map<String, Object> params);
    List<Map<String, Object>> parseFields(Dataset dataset);
}
