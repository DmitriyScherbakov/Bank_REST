package com.example.bankcards.controller;

import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Управление банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    @GetMapping("/my")
    @Operation(summary = "Получить мои карты", description = "Возвращает список карт текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<List<CardResponse>> getMyCards(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(null);
        }
        List<Card> cards = cardService.getUserActiveCards(authentication.getName());
        List<CardResponse> response = cards.stream()
                .map(cardMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/active")
    @Operation(summary = "Получить активные карты", description = "Возвращает только активные карты пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активные карты получены успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<List<CardResponse>> getMyActiveCards(Authentication authentication) {
        List<Card> cards = cardService.getUserActiveCards(authentication.getName());
        List<CardResponse> response = cards.stream()
                .map(cardMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Получить карту по ID", description = "Возвращает детали конкретной карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта найдена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<CardResponse> getCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId,
            Authentication authentication) {
        
        try {
            Card card = cardService.getCardById(cardId, authentication.getName());
            CardResponse response = cardMapper.toResponse(card);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать новую карту", description = "Создает новую банковскую карту для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта создана успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или превышен лимит карт"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<?> createCard(
            @Valid @RequestBody CreateCardRequest request,
            Authentication authentication) {
        
                    try {
                Card card = cardService.createCard(authentication.getName(), request.cardHolder());
                CardResponse response = cardMapper.toResponse(card);
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }
    }

    @PutMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать карту", description = "Блокирует указанную карту пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Карта уже заблокирована")
    })
    public ResponseEntity<String> blockCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId,
            Authentication authentication) {
        
        try {
            cardService.blockCard(cardId, authentication.getName());
            return ResponseEntity.ok("Card blocked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами", description = "Переводит деньги между картами одного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или некорректные данные"),
            @ApiResponse(responseCode = "404", description = "Одна из карт не найдена")
    })
    public ResponseEntity<String> transferMoney(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        
        try {
            cardService.transferMoney(
                    request.fromCardId(),
                    request.toCardId(),
                    request.amount(),
                    authentication.getName()
            );
            return ResponseEntity.ok("Transfer completed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}