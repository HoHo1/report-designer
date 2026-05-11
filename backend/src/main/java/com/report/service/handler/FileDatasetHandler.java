package com.report.service.handler;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.report.entity.Dataset;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Component
public class FileDatasetHandler implements DatasetHandler {

    private static final Map<String, List<Map<String, Object>>> fileDataCache = new HashMap<>();

    @Override
    public List<Map<String, Object>> fetchData(Dataset dataset, Map<String, Object> params) {
        JSONObject config = JSON.parseObject(dataset.getConfig());
        String fileId = config.getString("fileId");
        String sheetName = config.getString("sheetName");

        List<Map<String, Object>> allData = fileDataCache.get(fileId);
        if (allData == null) {
            return new ArrayList<>();
        }

        int page = params.get("page") != null ? (int) params.get("page") : 1;
        int pageSize = params.get("pageSize") != null ? (int) params.get("pageSize") : 100;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());

        if (start >= allData.size()) {
            return new ArrayList<>();
        }

        return allData.subList(start, end);
    }

    @Override
    public List<Map<String, Object>> parseFields(Dataset dataset) {
        JSONObject config = JSON.parseObject(dataset.getConfig());
        String fileId = config.getString("fileId");

        List<Map<String, Object>> allData = fileDataCache.get(fileId);
        if (allData == null || allData.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> firstRow = allData.get(0);
        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            Map<String, Object> field = new HashMap<>();
            field.put("name", entry.getKey());
            field.put("type", entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "String");
            fields.add(field);
        }
        return fields;
    }

    public void uploadFile(String fileId, MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<Map<String, Object>> data;

        if ("csv".equals(extension)) {
            data = parseCsv(file.getInputStream());
        } else if ("xlsx".equals(extension) || "xls".equals(extension)) {
            data = parseExcel(file.getInputStream());
        } else {
            throw new RuntimeException("不支持的文件格式: " + extension);
        }

        fileDataCache.put(fileId, data);
    }

    private List<Map<String, Object>> parseExcel(InputStream inputStream) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            EasyExcel.read(inputStream, new ReadListener<Map<Integer, String>>() {
                private Map<String, Object> headerMap;

                @Override
                public void invokeHead(Map<Integer, Object> headMap, AnalysisContext context) {
                    headerMap = new LinkedHashMap<>();
                    headMap.forEach((k, v) -> headerMap.put(k.toString(), v != null ? v.toString() : ""));
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    headerMap.forEach((k, v) -> {
                        String value = data.get(Integer.parseInt(k));
                        row.put(v.toString(), value);
                    });
                    result.add(row);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败: " + e.getMessage(), e);
        }

        return result;
    }

    private List<Map<String, Object>> parseCsv(InputStream inputStream) {
        List<Map<String, Object>> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result;
            }

            String[] headers = parseCsvLine(headerLine);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = parseCsvLine(line);
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i]);
                }
                result.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV解析失败: " + e.getMessage(), e);
        }

        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());

        return result.toArray(new String[0]);
    }

    public void clearCache(String fileId) {
        fileDataCache.remove(fileId);
    }
}
