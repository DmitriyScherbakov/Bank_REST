package com.example.bankcards.controller;

import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

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

    private Card card;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        User user = new User("ivan_ivanov", "password", "ivan@ivanov.com", Role.USER);
        user.setId(1L);

        card = new Card("encrypted123", "**** **** **** 1234", "Иван Иванов", 
                LocalDate.now().plusYears(3), user);
        card.setId(1L);
        card.setBalance(new BigDecimal("1000.00"));

        cardResponse = new CardResponse(
                1L,
                "**** **** **** 1234",
                "Иван Иванов",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00"),
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_Success() throws Exception {
        List<Card> cardList = List.of(card);
        Page<Card> cardPage = new PageImpl<>(cardList, PageRequest.of(0, 20), 1);

        when(cardService.getAllCards(any(PageRequest.class))).thenReturn(cardPage);
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].cardHolder").value("Иван Иванов"));
    }

    @Test
    void getAllCards_AccessDenied_NotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCardForUser_Success() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        when(cardService.createCard("ivan_ivanov", "Иван Иванов")).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(post("/api/admin/cards/ivan_ivanov")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolder").value("Иван Иванов"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCardForUser_UserNotFound_ReturnsBadRequest() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        when(cardService.createCard("nonexistent_user", "Иван Иванов"))
                .thenThrow(new RuntimeException("User not found: nonexistent_user"));

        mockMvc.perform(post("/api/admin/cards/nonexistent_user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: User not found: nonexistent_user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_Success() throws Exception {
        doNothing().when(cardService).activateCard(1L);

        mockMvc.perform(put("/api/admin/cards/1/activate")
)
                .andExpect(status().isOk())
                .andExpect(content().string("Card activated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_CardNotFound_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Card not found"))
                .when(cardService).activateCard(99L);

        mockMvc.perform(put("/api/admin/cards/99/activate")
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Card not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_Success() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/api/admin/cards/1")
        )
                .andExpect(status().isOk())
                .andExpect(content().string("Card deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_CardNotFound_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Card not found"))
                .when(cardService).deleteCard(99L);

        mockMvc.perform(delete("/api/admin/cards/99")
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Card not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCardStatuses_Success() throws Exception {
        doNothing().when(cardService).updateCardStatus();

        mockMvc.perform(post("/api/admin/cards/update-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Card statuses updated successfully"));
    }

    @Test
    void createCardForUser_AccessDenied_NotAdmin() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        mockMvc.perform(post("/api/admin/cards/ivan_ivanov")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void activateCard_AccessDenied_NotAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/cards/1/activate")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCard_AccessDenied_NotAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCardStatuses_AccessDenied_NotAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/cards/update-status"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCards_NoAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCardForUser_NoAuthentication() throws Exception {
        CreateCardRequest request = new CreateCardRequest("Иван Иванов");

        mockMvc.perform(post("/api/admin/cards/ivan_ivanov")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}