package com.report.service.handler;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.report.entity.Dataset;
import com.report.mapper.DatasetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.*;

@Component
public class SqlDatasetHandler implements DatasetHandler {

    @Autowired
    private DatasetMapper datasetMapper;

    private final Map<String, DataSource> dataSourceCache = new HashMap<>();

    @Override
    public List<Map<String, Object>> fetchData(Dataset dataset, Map<String, Object> params) {
        JSONObject config = JSON.parseObject(dataset.getConfig());
        String sql = replaceParameters(config.getString("sql"), params);
        int timeout = config.getIntValue("timeout", 10);

        try (Connection conn = getConnection(dataset);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setQueryTimeout(timeout);
            var rs = ps.executeQuery();
            List<Map<String, Object>> result = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
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

    private DataSource getConnection(Dataset dataset) {
        JSONObject config = JSON.parseObject(dataset.getConfig());
        String dbType = config.getString("dbType");
        String host = config.getString("host");
        int port = config.getInteger("port");
        String database = config.getString("database");
        String username = config.getString("username");
        String password = config.getString("password");

        String cacheKey = dataset.getId();
        if (!dataSourceCache.containsKey(cacheKey)) {
            DruidDataSource ds = new DruidDataSource();
            String jdbcUrl = getJdbcUrl(dbType, host, port, database);
            
            ds.setDriverClassName(getDriverClass(dbType));
            ds.setUrl(jdbcUrl);
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setInitialSize(1);
            ds.setMaxActive(5);
            
            dataSourceCache.put(cacheKey, ds);
        }
        
        return dataSourceCache.get(cacheKey);
    }

    private String getJdbcUrl(String dbType, String host, int port, String database) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8", host, port, database);
            case "POSTGRESQL" -> String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "ORACLE" -> String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "SQLSERVER" -> String.format("jdbc:sqlserver://%s:%d;DatabaseName=%s", host, port, database);
            default -> throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        };
    }

    private String getDriverClass(String dbType) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL" -> "org.postgresql.Driver";
            case "ORACLE" -> "oracle.jdbc.OracleDriver";
            case "SQLSERVER" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default -> throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        };
    }

    private String replaceParameters(String sql, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return sql;
        }
        
        String result = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "#{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, "'" + value.replace("'", "''") + "'");
        }
        return result;
    }
}
