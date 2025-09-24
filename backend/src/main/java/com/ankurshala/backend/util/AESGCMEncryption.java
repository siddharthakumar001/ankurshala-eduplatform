package com.ankurshala.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM encryption utility for sensitive data like bank account numbers.
 * Uses AES-GCM mode for authenticated encryption with a 256-bit key.
 */
public class AESGCMEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits

    @Value("${app.encryption.bank-key:DEFAULT_BANK_ENCRYPTION_KEY_CHANGE_IN_PRODUCTION_32_CHARS_MIN}")
    private String bankEncryptionKey;

    /**
     * Encrypts plaintext using AES-GCM with a random IV.
     *
     * @param plaintext The text to encrypt
     * @return Base64 encoded encrypted data (IV + ciphertext + tag)
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            return plaintext;
        }

        try {
            // Generate a random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            // Get the secret key
            SecretKey secretKey = getSecretKey();

            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            // Encrypt the plaintext
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext for storage
            byte[] encryptedData = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, encryptedData, GCM_IV_LENGTH, ciphertext.length);

            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts Base64 encoded encrypted data using AES-GCM.
     *
     * @param encryptedData Base64 encoded encrypted data (IV + ciphertext + tag)
     * @return Decrypted plaintext
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            return encryptedData;
        }

        try {
            // Decode from Base64
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            // Extract IV and ciphertext
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(decodedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            // Get the secret key
            SecretKey secretKey = getSecretKey();

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            // Decrypt the ciphertext
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generates a secret key from the configured encryption key.
     *
     * @return SecretKey for AES encryption
     */
    private SecretKey getSecretKey() {
        try {
            // Use the first 32 bytes of the configured key (256 bits)
            byte[] keyBytes = bankEncryptionKey.getBytes(StandardCharsets.UTF_8);
            
            // Ensure we have exactly 32 bytes for AES-256
            byte[] key = new byte[32];
            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, key, 0, 32);
            } else {
                // If key is too short, pad with zeros (not recommended for production)
                System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
            }

            return new SecretKeySpec(key, ALGORITHM);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    /**
     * Generates a new random AES-256 key for testing purposes.
     * This method should only be used for generating new keys during setup.
     *
     * @return Base64 encoded random AES-256 key
     */
    public static String generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate random key", e);
        }
    }
}
