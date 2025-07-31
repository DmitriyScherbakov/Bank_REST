package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardNumberGenerator cardNumberGenerator;

    @Autowired
    private EncryptionService encryptionService;

    public Card createCard(String username, String cardHolder) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        long cardCount = cardRepository.countByOwnerId(user.getId());
        if (cardCount >= 5) {
            throw new RuntimeException("Maximum number of cards reached (5)");
        }

        String cardNumber;
        String encryptedNumber;
        int attempts = 0;
        do {
            cardNumber = cardNumberGenerator.generateCardNumber();
            encryptedNumber = encryptionService.encrypt(cardNumber);
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique card number after 10 attempts");
            }
        } while (cardRepository.existsByEncryptedCardNumber(encryptedNumber));

        String maskedNumber = encryptionService.createMask(cardNumber);
        LocalDate expiryDate = cardNumberGenerator.generateExpiryDate();

        Card card = new Card(encryptedNumber, maskedNumber, cardHolder, expiryDate, user);
        
        return cardRepository.save(card);
    }

    public Page<Card> getUserCards(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    public List<Card> getUserActiveCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardRepository.findActiveCardsByOwner(user.getId());
    }

    public Card getCardById(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        return card;
    }

    public void blockCard(Long cardId, String username) {
        Card card = getCardById(cardId, username);
        
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void transferMoney(Long fromCardId, Long toCardId, BigDecimal amount, String username) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        Card fromCard = getCardById(fromCardId, username);
        Card toCard = getCardById(toCardId, username);

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Both cards must be active for transfer");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public void updateCardStatus() {
        LocalDate now = LocalDate.now();
        List<Card> allCards = cardRepository.findAll();
        
        for (Card card : allCards) {
            if (card.getExpiryDate().isBefore(now) && card.getStatus() != CardStatus.EXPIRED) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        }
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
                
        cardRepository.delete(card);
    }
}