package com.example.bankcards.controller;

import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private CardMapper cardMapper;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Card card;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("ivan_ivanov");
        user.setEmail("ivan@ivanov.com");
        user.setRole(Role.USER);

        card = new Card();
        card.setId(1L);
        card.setEncryptedCardNumber("encrypted123");
        card.setMaskedCardNumber("**** **** **** 1234");
        card.setCardHolder("Иван Иванов");
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setBalance(new BigDecimal("1000.00"));
        card.setStatus(CardStatus.ACTIVE);
        card.setCreatedAt(LocalDateTime.now());
        cardResponse = new CardResponse(
                1L,
                "**** **** **** 1234",
                "Иван Иванов",
                LocalDate.of(2027, 1, 1),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00"),
                LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        );
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void getMyCards_Success() throws Exception {
        List<Card> cardList = List.of(card);

        when(cardService.getUserActiveCards(eq("ivan_ivanov")))
                .thenReturn(cardList);
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        try {
            mockMvc.perform(get("/api/cards/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 1234"))
                    .andExpect(jsonPath("$[0].cardHolder").value("Иван Иванов"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void getMyActiveCards_Success() throws Exception {
        when(cardService.getUserActiveCards(eq("ivan_ivanov"))).thenReturn(List.of(card));
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        mockMvc.perform(get("/api/cards/my/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].cardHolder").value("Иван Иванов"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void getCard_Success() throws Exception {
        when(cardService.getCardById(eq(1L), eq("ivan_ivanov"))).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardHolder").value("Иван Иванов"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void getCard_NotFound() throws Exception {
        when(cardService.getCardById(eq(1L), eq("ivan_ivanov")))
                .thenThrow(new RuntimeException("Card not found"));

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void createCard_Success() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        when(cardService.createCard(eq("ivan_ivanov"), eq("Иван Иванов"))).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolder").value("Иван Иванов"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void createCard_MaxCardsReached_ReturnsBadRequest() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        when(cardService.createCard(eq("ivan_ivanov"), eq("Иван Иванов")))
                .thenThrow(new RuntimeException("Maximum number of cards reached (5)"));

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Maximum number of cards reached (5)"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void blockCard_Success() throws Exception {
        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(content().string("Card blocked successfully"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void blockCard_AlreadyBlocked_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Card is already blocked"))
                .when(cardService).blockCard(eq(1L), eq("ivan_ivanov"));

        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Card is already blocked"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void transferMoney_Success() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer completed successfully"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void transferMoney_InsufficientFunds_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("2000.00"));

        doThrow(new RuntimeException("Insufficient funds"))
                .when(cardService).transferMoney(eq(1L), eq(2L), eq(new BigDecimal("2000.00")), eq("ivan_ivanov"));

        mockMvc.perform(post("/api/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds"));
    }

    @Test
    @WithMockUser(username = "ivan_ivanov")
    void transferMoney_NegativeAmount_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.amount").value("Amount must be greater than 0"));
    }

    @Test
    void getMyCards_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cards/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCard_Unauthorized() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}