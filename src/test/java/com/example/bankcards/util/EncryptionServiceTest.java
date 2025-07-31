package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "encryptionKey", "testKey1234567890");
    }

    @Test
    void encrypt_ValidText_ReturnsEncryptedString() {
        String plainText = "1234567812345678";
        
        String encrypted = encryptionService.encrypt(plainText);
        
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted).isNotEmpty();
    }

    @Test
    void decrypt_EncryptedText_ReturnsOriginalText() {
        String originalText = "1234567812345678";
        String encrypted = encryptionService.encrypt(originalText);
        
        String decrypted = encryptionService.decrypt(encrypted);
        
        assertThat(decrypted).isEqualTo(originalText);
    }

    @Test
    void createMask_StandardCardNumber_ReturnsMaskedFormat() {
        String cardNumber = "1234567812345678";
        
        String mask = encryptionService.createMask(cardNumber);
        
        assertThat(mask).isEqualTo("**** **** **** 5678");
    }

    @Test
    void createMask_CardNumberWithSpaces_ReturnsMaskedFormat() {
        String cardNumber = "1234 5678 1234 5678";
        
        String mask = encryptionService.createMask(cardNumber);
        
        assertThat(mask).isEqualTo("**** **** **** 5678");
    }

    @Test
    void formatCardNumber_StandardNumber_FormatsWithSpaces() {
        String cardNumber = "1234567812345678";
        
        String formatted = encryptionService.formatCardNumber(cardNumber);
        
        assertThat(formatted).isEqualTo("1234 5678 1234 5678");
    }

    @Test
    void encryptDecrypt_MultipleTexts_WorksCorrectly() {
        String[] testTexts = {
            "1234567812345678",
            "4000123456789012",
            "5555444433332222",
            "ivan@ivanov.com"
        };

        for (String text : testTexts) {
            String encrypted = encryptionService.encrypt(text);
            String decrypted = encryptionService.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(text);
        }
    }

    @Test
    void createMask_PreservesLastFourDigits() {
        String[] testNumbers = {
            "1111222233334444",
            "5555666677778888",
            "9999000011112222"
        };

        for (String number : testNumbers) {
            String mask = encryptionService.createMask(number);
            String lastFour = number.substring(number.length() - 4);
            assertThat(mask).endsWith(lastFour);
        }
    }
}