package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    @Value("${encryption.key:mySecretEncryptionKey123456}")
    private String encryptionKey;

    private SecretKey getSecretKey() {
        byte[] key = encryptionKey.getBytes();
        if (key.length < 16) {
            byte[] paddedKey = new byte[16];
            System.arraycopy(key, 0, paddedKey, 0, key.length);
            key = paddedKey;
        } else if (key.length > 16) {
            byte[] truncatedKey = new byte[16];
            System.arraycopy(key, 0, truncatedKey, 0, 16);
            key = truncatedKey;
        }
        return new SecretKeySpec(key, ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    public String createMask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        if (cleanNumber.length() < 4) {
            return "****";
        }

        if (cleanNumber.length() == 16) {
            String lastFour = cleanNumber.substring(12);
            return "**** **** **** " + lastFour;
        }
        
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        int starsCount = cleanNumber.length() - 4;
        StringBuilder mask = new StringBuilder();
        
        for (int i = 0; i < starsCount; i++) {
            mask.append("*");
        }
        
        return mask.toString() + lastFour;
    }

    public String formatCardNumber(String cardNumber) {
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < cleanNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cleanNumber.charAt(i));
        }
        
        return formatted.toString();
    }
}