package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberGeneratorTest {

    private CardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CardNumberGenerator();
    }

    @Test
    void generateCardNumber_ReturnsValidLength() {
        String cardNumber = generator.generateCardNumber();
        
        assertThat(cardNumber).hasSize(16);
        assertThat(cardNumber).matches("\\d{16}");
    }

    @Test
    void generateCardNumber_StartsWithValidPrefix() {
        String cardNumber = generator.generateCardNumber();
        String prefix = cardNumber.substring(0, 4);
        
        assertThat(prefix).isIn("4000", "4001", "4002", "4003", 
                                "5000", "5001", "5002", "5003",
                                "2200", "2201", "2202", "2203");
    }

    @Test
    void generateCardNumber_PassesLuhnValidation() {
        for (int i = 0; i < 10; i++) {
            String cardNumber = generator.generateCardNumber();
            assertThat(generator.isValidCardNumber(cardNumber)).isTrue();
        }
    }

    @Test
    void generateExpiryDate_IsInFuture() {
        LocalDate expiryDate = generator.generateExpiryDate();
        LocalDate now = LocalDate.now();
        
        assertThat(expiryDate).isAfter(now);
        assertThat(expiryDate).isBefore(now.plusYears(7));
    }

    @Test
    void isValidCardNumber_ValidCard_ReturnsTrue() {
        String validCard = "4000000000000002";
        
        assertThat(generator.isValidCardNumber(validCard)).isTrue();
    }

    @Test
    void isValidCardNumber_InvalidCard_ReturnsFalse() {
        String invalidCard = "4000000000000001";
        
        assertThat(generator.isValidCardNumber(invalidCard)).isFalse();
    }

    @Test
    void getCardType_VisaCard_ReturnsVisa() {
        assertThat(generator.getCardType("4000123456789012")).isEqualTo("VISA");
        assertThat(generator.getCardType("4001123456789012")).isEqualTo("VISA");
    }

    @Test
    void getCardType_MastercardCard_ReturnsMastercard() {
        assertThat(generator.getCardType("5000123456789012")).isEqualTo("MASTERCARD");
        assertThat(generator.getCardType("5001123456789012")).isEqualTo("MASTERCARD");
    }

    @Test
    void getCardType_MirCard_ReturnsMir() {
        assertThat(generator.getCardType("2200123456789012")).isEqualTo("MIR");
        assertThat(generator.getCardType("2201123456789012")).isEqualTo("MIR");
    }

    @Test
    void generateMultipleCards_AllUnique() {
        java.util.Set<String> generatedNumbers = new java.util.HashSet<>();
        
        for (int i = 0; i < 50; i++) {
            String cardNumber = generator.generateCardNumber();
            assertThat(generatedNumbers).doesNotContain(cardNumber);
            generatedNumbers.add(cardNumber);
        }
    }
}