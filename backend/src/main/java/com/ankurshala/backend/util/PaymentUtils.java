package com.ankurshala.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentUtils {

    public boolean verifyRazorpaySignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = calculateHmacSha256(payload, secret);
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying Razorpay signature: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyStripeSignature(String payload, String signature, String secret) {
        try {
            // Stripe signature format: t=timestamp,v1=signature
            String[] parts = signature.split(",");
            String timestamp = null;
            String v1Signature = null;

            for (String part : parts) {
                if (part.startsWith("t=")) {
                    timestamp = part.substring(2);
                } else if (part.startsWith("v1=")) {
                    v1Signature = part.substring(3);
                }
            }

            if (timestamp == null || v1Signature == null) {
                return false;
            }

            String signedPayload = timestamp + "." + payload;
            String expectedSignature = calculateHmacSha256(signedPayload, secret);
            return v1Signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying Stripe signature: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyPayuSignature(Map<String, String> params, String secret) {
        try {
            // PayU signature verification
            String hashString = params.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("hash") && !entry.getValue().isEmpty())
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("|"));
            hashString += "|" + secret;

            String calculatedHash = calculateSha512(hashString);
            String receivedHash = params.get("hash");
            return calculatedHash.equals(receivedHash);
        } catch (Exception e) {
            log.error("Error verifying PayU signature: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyPaytmSignature(Map<String, String> params, String secret) {
        try {
            // Paytm signature verification
            String hashString = params.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("CHECKSUMHASH") && !entry.getValue().isEmpty())
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            hashString += "&" + secret;

            String calculatedHash = calculateSha256(hashString);
            String receivedHash = params.get("CHECKSUMHASH");
            return calculatedHash.equals(receivedHash);
        } catch (Exception e) {
            log.error("Error verifying Paytm signature: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyPhonepeSignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = calculateHmacSha256(payload, secret);
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying PhonePe signature: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifyGooglePaySignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = calculateHmacSha256(payload, secret);
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying Google Pay signature: {}", e.getMessage());
            return false;
        }
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String calculateSha256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    private String calculateSha512(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    public String generateOrderId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    public String generateTransactionId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    public String formatAmount(Integer amountCents) {
        return String.format("%.2f", amountCents / 100.0);
    }

    public Integer parseAmount(String amount) {
        try {
            double amountDouble = Double.parseDouble(amount);
            return (int) Math.round(amountDouble * 100);
        } catch (NumberFormatException e) {
            log.error("Error parsing amount: {}", amount);
            return 0;
        }
    }
}
