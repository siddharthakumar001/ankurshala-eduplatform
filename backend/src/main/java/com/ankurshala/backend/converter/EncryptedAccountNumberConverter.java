package com.ankurshala.backend.converter;

import com.ankurshala.backend.util.AESGCMEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for encrypting/decrypting bank account numbers.
 * Uses AES-GCM encryption to securely store sensitive banking information.
 */
@Converter
@Component
public class EncryptedAccountNumberConverter implements AttributeConverter<String, String> {

    private static AESGCMEncryption encryption;

    /**
     * Static setter for dependency injection.
     * This is needed because JPA converters are instantiated by the JPA provider,
     * not by Spring, so we need to inject the encryption service statically.
     */
    @Autowired
    public void setEncryption(AESGCMEncryption encryption) {
        EncryptedAccountNumberConverter.encryption = encryption;
    }

    /**
     * Converts the entity attribute value to the database column representation.
     * Encrypts the account number before storing it in the database.
     *
     * @param attribute The entity attribute value to be converted
     * @return The encrypted account number for database storage
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.trim().isEmpty()) {
            return attribute;
        }

        if (encryption == null) {
            throw new IllegalStateException("AESGCMEncryption service not initialized. Check Spring configuration.");
        }

        return encryption.encrypt(attribute);
    }

    /**
     * Converts the database column value to the entity attribute representation.
     * Decrypts the account number when loading from the database.
     *
     * @param dbData The database column value to be converted
     * @return The decrypted account number for entity use
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return dbData;
        }

        if (encryption == null) {
            throw new IllegalStateException("AESGCMEncryption service not initialized. Check Spring configuration.");
        }

        return encryption.decrypt(dbData);
    }
}
