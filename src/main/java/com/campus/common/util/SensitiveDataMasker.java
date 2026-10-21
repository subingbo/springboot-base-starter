package com.campus.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SensitiveDataMasker {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final Set<String> EXACT_NAMES = Set.of(
            "password", "oldpassword", "newpassword", "confirmpassword",
            "token", "accesstoken", "refreshtoken", "authorization",
            "secret", "clientsecret", "appsecret"
    );

    private SensitiveDataMasker() {
    }

    public static String mask(Object value) {
        return mask(value, 4000);
    }

    public static String mask(Object value, int maxLength) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode node = MAPPER.valueToTree(value);
            maskNode(node);
            return truncate(MAPPER.writeValueAsString(node), maxLength);
        } catch (Exception ex) {
            return truncate(String.valueOf(value), maxLength);
        }
    }

    public static String maskJson(String json, int maxLength) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            maskNode(node);
            return truncate(MAPPER.writeValueAsString(node), maxLength);
        } catch (Exception ex) {
            return truncate(json, maxLength);
        }
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private static void maskNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (isSensitive(entry.getKey())) {
                    objectNode.put(entry.getKey(), "***");
                } else {
                    maskNode(entry.getValue());
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(SensitiveDataMasker::maskNode);
        }
    }

    private static boolean isSensitive(String fieldName) {
        String name = fieldName.toLowerCase(Locale.ROOT);
        return EXACT_NAMES.contains(name)
                || name.endsWith("password")
                || name.endsWith("token")
                || name.contains("secret");
    }
}
