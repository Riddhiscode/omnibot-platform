package com.omnibot.rewards.repository;

import com.omnibot.rewards.entity.LoyaltyLedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedgerEntry, Long> {

    boolean existsByConfirmationTokenId(String confirmationTokenId);

    List<LoyaltyLedgerEntry> findByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);
}
