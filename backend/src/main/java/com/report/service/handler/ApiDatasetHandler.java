package com.report.service.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.report.entity.Dataset;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class ApiDatasetHandler implements DatasetHandler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<Map<String, Object>> fetchData(Dataset dataset, Map<String, Object> params) {
        JSONObject config = JSON.parseObject(dataset.getConfig());
        String url = config.getString("url");
        String method = config.getString("method");
        JSONObject headers = config.getJSONObject("headers");
        JSONObject requestParams = config.getJSONObject("params");
        String dataPath = config.getString("dataPath");
        int timeout = config.getIntValue("timeout", 10);

        try {
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            if (headers != null) {
                headers.forEach((key, value) -> httpHeaders.set(key, value.toString()));
            }

            Map<String, Object> body = new HashMap<>();
            if (requestParams != null) {
                requestParams.forEach(body::put);
            }
            if (params != null) {
                body.putAll(params);
            }

            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                new org.springframework.http.HttpEntity<>(body, httpHeaders);

            org.springframework.http.ResponseEntity<String> response;
            if ("POST".equalsIgnoreCase(method)) {
                response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, String.class);
            } else {
                response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            }

            String responseBody = response.getBody();
            return parseJsonData(responseBody, dataPath);
        } catch (Exception e) {
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> parseFields(Dataset dataset) {
        List<Map<String, Object>> data = fetchData(dataset, new HashMap<>());
        if (data.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> firstRow = data.get(0);
        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            Map<String, Object> field = new HashMap<>();
            field.put("name", entry.getKey());
            field.put("type", entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "String");
            fields.add(field);
        }
        return fields;
    }

    private List<Map<String, Object>> parseJsonData(String jsonStr, String dataPath) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            JSONObject json = JSON.parseObject(jsonStr);
            
            if (dataPath == null || dataPath.isEmpty()) {
                if (json.containsKey("data")) {
                    json = json.getJSONObject("data");
                }
            } else {
                String[] paths = dataPath.split("\\.");
                for (String path : paths) {
                    if (json.containsKey(path)) {
                        json = json.getJSONObject(path);
                    }
                }
            }

            if (json instanceof JSONArray) {
                JSONArray array = (JSONArray) json;
                for (int i = 0; i < array.size(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    result.add(new LinkedHashMap<>(item));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
        }
        
        return result;
    }
}
