package com.example.bankcards.integration;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardIntegrationTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private CardService cardService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CardRepository cardRepository;

    private String testUsername = "ivan_ivanov";
    private String testPassword = "password123";
    private String testEmail = "ivan@ivanov.com";

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fullCardLifecycle_Success() {
        RegisterRequest registerRequest = new RegisterRequest(testUsername, testPassword, testEmail);
        authService.registerUser(registerRequest);

        LoginRequest loginRequest = new LoginRequest(testUsername, testPassword);
        LoginResponse loginResponse = authService.loginUser(loginRequest);
        
        assertThat(loginResponse.username()).isEqualTo(testUsername);
        assertThat(loginResponse.role()).isEqualTo(Role.USER);
        assertThat(loginResponse.token()).isNotEmpty();

        Card card = cardService.createCard(testUsername, "Иван Иванов");
        
        assertThat(card).isNotNull();
        assertThat(card.getCardHolder()).isEqualTo("Иван Иванов");
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(card.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(card.getMaskedCardNumber()).matches("\\*{4} \\*{4} \\*{4} \\d{4}");

        cardService.blockCard(card.getId(), testUsername);
        Card blockedCard = cardService.getCardById(card.getId(), testUsername);
        assertThat(blockedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);

        cardService.activateCard(card.getId());
        Card activatedCard = cardService.getCardById(card.getId(), testUsername);
        assertThat(activatedCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    void cardTransfer_Success() {
        RegisterRequest registerRequest = new RegisterRequest(testUsername, testPassword, testEmail);
        authService.registerUser(registerRequest);

        Card card1 = cardService.createCard(testUsername, "Иван Иванов");
        Card card2 = cardService.createCard(testUsername, "Иван Иванов");

        card1.setBalance(new BigDecimal("1000.00"));
        card2.setBalance(new BigDecimal("500.00"));
        cardRepository.save(card1);
        cardRepository.save(card2);

        cardService.transferMoney(card1.getId(), card2.getId(), new BigDecimal("200.00"), testUsername);

        Card updatedCard1 = cardService.getCardById(card1.getId(), testUsername);
        Card updatedCard2 = cardService.getCardById(card2.getId(), testUsername);

        assertThat(updatedCard1.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(updatedCard2.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    void maxCardsLimit_EnforcedCorrectly() {
        RegisterRequest registerRequest = new RegisterRequest(testUsername, testPassword, testEmail);
        authService.registerUser(registerRequest);

        for (int i = 0; i < 5; i++) {
            cardService.createCard(testUsername, "Иван Иванов " + i);
        }

        assertThatThrownBy(() -> cardService.createCard(testUsername, "Лишняя карта"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Maximum number of cards reached (5)");

        long cardCount = cardRepository.countByOwnerId(
                userRepository.findByUsername(testUsername).get().getId());
        assertThat(cardCount).isEqualTo(5);
    }

    @Test
    void cardAccess_DeniedForWrongUser() {
        RegisterRequest registerRequest1 = new RegisterRequest("ivan_ivanov", testPassword, "ivan@ivanov.com");
        RegisterRequest registerRequest2 = new RegisterRequest("petr_petrov", testPassword, "petr@petrov.com");
        
        authService.registerUser(registerRequest1);
        authService.registerUser(registerRequest2);

        Card card = cardService.createCard("ivan_ivanov", "Иван Иванов");

        assertThatThrownBy(() -> cardService.getCardById(card.getId(), "petr_petrov"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");
    }
}