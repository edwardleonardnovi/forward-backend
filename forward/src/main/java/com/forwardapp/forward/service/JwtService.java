package com.forwardapp.forward.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class JwtService {

    private static final String SECRET = "my-very-secret-key";

    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        long expiry = now + 1000 * 60 * 60;

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = String.format("{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}", username, now / 1000, expiry / 1000);
        String payloadEncoded = Base64.getUrlEncoder().encodeToString(payload.getBytes());

        String signature = hmacSha256(header + "." + payloadEncoded, SECRET);

        return header + "." + payloadEncoded + "." + signature;
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return payloadJson.split("\"sub\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String expectedSignature = hmacSha256(header + "." + payload, SECRET);
            if (!expectedSignature.equals(signature)) return false;

            String username = extractUsername(token);
            if (username == null || !username.equals(userDetails.getUsername())) return false;

            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            String expString = payloadJson.split("\"exp\":")[1].split("}")[0].replaceAll("[^0-9]", "");
            long exp = Long.parseLong(expString) * 1000;

            return System.currentTimeMillis() < exp;

        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSha256(String data, String secret) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HMAC SHA256 signature", e);
        }
    }
}
