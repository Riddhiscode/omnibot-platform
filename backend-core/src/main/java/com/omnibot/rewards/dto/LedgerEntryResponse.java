package com.omnibot.rewards.dto;

import com.omnibot.rewards.model.LedgerReason;

import java.time.Instant;

public record LedgerEntryResponse(
        Long id,
        int pointsDelta,
        LedgerReason reason,
        String vendorId,
        String description,
        Instant createdAt
) {}
