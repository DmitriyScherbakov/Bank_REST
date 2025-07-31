package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private static final String[] VISA_PREFIXES = {"4000", "4001", "4002", "4003"};
    private static final String[] MASTERCARD_PREFIXES = {"5000", "5001", "5002", "5003"};
    private static final String[] MIR_PREFIXES = {"2200", "2201", "2202", "2203"};
    
    private final Random random = new SecureRandom();

    public String generateCardNumber() {
        String[] allPrefixes = {
                VISA_PREFIXES[random.nextInt(VISA_PREFIXES.length)],
                MASTERCARD_PREFIXES[random.nextInt(MASTERCARD_PREFIXES.length)],
                MIR_PREFIXES[random.nextInt(MIR_PREFIXES.length)]
        };
        
        String prefix = allPrefixes[random.nextInt(allPrefixes.length)];
        StringBuilder cardNumber = new StringBuilder(prefix);
        
        while (cardNumber.length() < 15) {
            cardNumber.append(random.nextInt(10));
        }
        
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }

    public LocalDate generateExpiryDate() {
        LocalDate now = LocalDate.now();
        int yearsToAdd = 2 + random.nextInt(4);
        int monthsToAdd = random.nextInt(12);
        
        return now.plusYears(yearsToAdd).plusMonths(monthsToAdd);
    }

    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        
        try {
            return isValidLuhn(cardNumber);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int calculateLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }

    public String getCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "UNKNOWN";
        }
        
        String prefix = cardNumber.substring(0, 4);
        
        for (String visaPrefix : VISA_PREFIXES) {
            if (prefix.equals(visaPrefix)) {
                return "VISA";
            }
        }
        
        for (String mastercardPrefix : MASTERCARD_PREFIXES) {
            if (prefix.equals(mastercardPrefix)) {
                return "MASTERCARD";
            }
        }
        
        for (String mirPrefix : MIR_PREFIXES) {
            if (prefix.equals(mirPrefix)) {
                return "MIR";
            }
        }
        
        return "UNKNOWN";
    }
}