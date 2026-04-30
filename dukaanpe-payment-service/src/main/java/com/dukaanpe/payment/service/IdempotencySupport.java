package com.dukaanpe.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class IdempotencySupport {

    private final ObjectMapper objectMapper;

    public IdempotencySupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hashPayload(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to compute idempotency hash", ex);
        }
    }

    public String normalizeKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String value = idempotencyKey.trim();
        return value.isEmpty() ? null : value;
    }
}

