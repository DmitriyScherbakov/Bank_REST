package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "encrypted_card_number", nullable = false, unique = true)
    private String encryptedCardNumber;
    
    @Column(name = "masked_card_number", nullable = false, length = 19)
    private String maskedCardNumber;
    
    @Column(name = "card_holder", nullable = false, length = 100)
    private String cardHolder;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Card() {
        this.createdAt = LocalDateTime.now();
        this.balance = BigDecimal.ZERO;
        this.status = CardStatus.ACTIVE;
    }

    public Card(String encryptedCardNumber, String maskedCardNumber, String cardHolder, 
                LocalDate expiryDate, User owner) {
        this();
        this.encryptedCardNumber = encryptedCardNumber;
        this.maskedCardNumber = maskedCardNumber;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.owner = owner;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}