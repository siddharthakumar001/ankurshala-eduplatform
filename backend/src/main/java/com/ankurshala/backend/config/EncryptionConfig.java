package com.ankurshala.backend.config;

import com.ankurshala.backend.converter.EncryptedAccountNumberConverter;
import com.ankurshala.backend.util.AESGCMEncryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration class to ensure proper initialization of encryption components.
 * This ensures that the AES-GCM encryption service is available before
 * JPA converters try to use it.
 */
@Configuration
@Slf4j
public class EncryptionConfig {

    /**
     * Ensures the AESGCMEncryption bean is created and available.
     */
    @Bean
    public AESGCMEncryption aesGCMEncryption() {
        return new AESGCMEncryption();
    }

    /**
     * Ensures the EncryptedAccountNumberConverter is properly initialized
     * after the AESGCMEncryption service is available.
     */
    @Bean
    @DependsOn("aesGCMEncryption")
    public EncryptedAccountNumberConverter encryptedAccountNumberConverter(AESGCMEncryption encryption) {
        EncryptedAccountNumberConverter converter = new EncryptedAccountNumberConverter();
        converter.setEncryption(encryption);
        return converter;
    }
}
