package com.example.bankcards.controller;

import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Административные функции управления картами")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    @GetMapping("/cards")
    @Operation(summary = "Получить все карты", description = "Возвращает список всех карт в системе с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен успешно"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуются права администратора")
    })
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.getAllCards(pageable);
        Page<CardResponse> response = cards.map(cardMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cards/{username}")
    @Operation(summary = "Создать карту для пользователя", description = "Создает новую карту для указанного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта создана успешно"),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден или превышен лимит карт"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<?> createCardForUser(
            @Parameter(description = "Имя пользователя") @PathVariable String username,
            @Valid @RequestBody CreateCardRequest request) {
        
        try {
            Card card = cardService.createCard(username, request.cardHolder());
            CardResponse response = cardMapper.toResponse(card);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/cards/{cardId}/activate")
    @Operation(summary = "Активировать карту", description = "Активирует заблокированную карту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта активирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<String> activateCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {
        
        try {
            cardService.activateCard(cardId);
            return ResponseEntity.ok("Card activated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Удалить карту", description = "Полностью удаляет карту из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<String> deleteCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {
        
        try {
            cardService.deleteCard(cardId);
            return ResponseEntity.ok("Card deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cards/update-status")
    @Operation(summary = "Обновить статусы карт", description = "Обновляет статусы всех истекших карт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статусы обновлены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<String> updateCardStatuses() {
        cardService.updateCardStatus();
        return ResponseEntity.ok("Card statuses updated successfully");
    }
}