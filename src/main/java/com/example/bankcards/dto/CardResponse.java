package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        String maskedCardNumber,
        String cardHolder,
        LocalDate expiryDate,
        CardStatus status,
        BigDecimal balance,
        LocalDateTime createdAt
) {}