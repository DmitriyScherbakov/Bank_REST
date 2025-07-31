package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    Page<Card> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    List<Card> findByOwnerIdAndStatus(Long ownerId, CardStatus status);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
    
    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);
    
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<Card> findActiveCardsByOwner(@Param("ownerId") Long ownerId);
    
    long countByOwnerId(Long ownerId);
   
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.expiryDate BETWEEN CURRENT_DATE AND :expiryDate")
    List<Card> findCardsExpiringBefore(@Param("ownerId") Long ownerId, @Param("expiryDate") java.time.LocalDate expiryDate);
}