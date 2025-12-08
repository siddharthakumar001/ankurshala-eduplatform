package com.ankurshala.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

@Converter
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {
    
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) {
            return null;
        }
        // Simple JSON-like string representation
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : attribute.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        // Simple parsing - in production, use a proper JSON library
        Map<String, Object> map = new HashMap<>();
        String content = dbData.substring(1, dbData.length() - 1); // Remove {}
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].replaceAll("\"", "");
                String value = keyValue[1].replaceAll("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }
}
