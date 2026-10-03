package com.paytm.bookshow.util;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class IdempotencyUtil {
    public static UUID getIdempotencyKey() {
        return UUID.randomUUID();
    }

    public static String hash(List<String> seats) {
        try {
            Collections.sort(seats);
            ObjectMapper objectMapper = new ObjectMapper();
            String seatString = objectMapper.writeValueAsString(seats);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seatString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Hash "+e.getMessage());
        }

    }
}
