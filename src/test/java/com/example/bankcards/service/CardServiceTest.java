package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CardNumberGenerator cardNumberGenerator;
    
    @Mock
    private EncryptionService encryptionService;
    
    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        user = new User("ivan_ivanov", "password", "ivan@ivanov.com", Role.USER);
        user.setId(1L);
        
        card1 = new Card("encrypted123", "**** **** **** 1234", "Иван Иванов", LocalDate.now().plusYears(3), user);
        card1.setId(1L);
        card1.setBalance(new BigDecimal("1000.00"));
        
        card2 = new Card("encrypted456", "**** **** **** 5678", "Иван Иванов", LocalDate.now().plusYears(3), user);
        card2.setId(2L);
        card2.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void createCard_Success() {
        when(userRepository.findByUsername("ivan_ivanov")).thenReturn(Optional.of(user));
        when(cardRepository.countByOwnerId(1L)).thenReturn(2L);
        when(cardNumberGenerator.generateCardNumber()).thenReturn("1234567812345678");
        when(encryptionService.encrypt("1234567812345678")).thenReturn("encrypted123");
        when(cardRepository.existsByEncryptedCardNumber("encrypted123")).thenReturn(false);
        when(encryptionService.createMask("1234567812345678")).thenReturn("**** **** **** 5678");
        when(cardNumberGenerator.generateExpiryDate()).thenReturn(LocalDate.now().plusYears(3));
        when(cardRepository.save(any(Card.class))).thenReturn(card1);

        Card result = cardService.createCard("ivan_ivanov", "Иван Иванов");

        assertThat(result).isNotNull();
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_MaxCardsReached_ThrowsException() {
        when(userRepository.findByUsername("ivan_ivanov")).thenReturn(Optional.of(user));
        when(cardRepository.countByOwnerId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> cardService.createCard("ivan_ivanov", "Иван Иванов"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Maximum number of cards reached (5)");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getUserCards_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<Card> expectedPage = new PageImpl<>(List.of(card1, card2));
        
        when(userRepository.findByUsername("ivan_ivanov")).thenReturn(Optional.of(user));
        when(cardRepository.findByOwnerIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(expectedPage);

        Page<Card> result = cardService.getUserCards("ivan_ivanov", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(card1, card2);
    }

    @Test
    void getCardById_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        Card result = cardService.getCardById(1L, "ivan_ivanov");

        assertThat(result).isEqualTo(card1);
        assertThat(result.getCardHolder()).isEqualTo("Иван Иванов");
    }

    @Test
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any(Card.class))).thenReturn(card1);

        cardService.blockCard(1L, "ivan_ivanov");

        assertThat(card1.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card1);
    }

    @Test
    void transferMoney_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        cardService.transferMoney(1L, 2L, new BigDecimal("100.00"), "ivan_ivanov");

        assertThat(card1.getBalance()).isEqualByComparingTo(new BigDecimal("900.00"));
        assertThat(card2.getBalance()).isEqualByComparingTo(new BigDecimal("600.00"));
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferMoney_InsufficientFunds_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThatThrownBy(() -> cardService.transferMoney(1L, 2L, new BigDecimal("2000.00"), "ivan_ivanov"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    void transferMoney_NegativeAmount_ThrowsException() {
        assertThatThrownBy(() -> cardService.transferMoney(1L, 2L, new BigDecimal("-100.00"), "ivan_ivanov"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Transfer amount must be positive");
    }

    @Test
    void transferMoney_CardNotActive_ThrowsException() {
        card1.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThatThrownBy(() -> cardService.transferMoney(1L, 2L, new BigDecimal("100.00"), "ivan_ivanov"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Both cards must be active for transfer");
    }

    @Test
    void updateCardStatus_ExpiresCards() {
        Card expiredCard = new Card("enc", "mask", "Иван Иванов", LocalDate.now().minusDays(1), user);
        expiredCard.setStatus(CardStatus.ACTIVE);
        
        when(cardRepository.findAll()).thenReturn(List.of(expiredCard));

        cardService.updateCardStatus();

        assertThat(expiredCard.getStatus()).isEqualTo(CardStatus.EXPIRED);
        verify(cardRepository).save(expiredCard);
    }

    @Test
    void deleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card1);
    }

    @Test
    void deleteCard_NotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Card not found");
    }
}