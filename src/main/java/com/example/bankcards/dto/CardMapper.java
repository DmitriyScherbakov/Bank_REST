package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getMaskedCardNumber(),
                card.getCardHolder(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt()
        );
    }
}