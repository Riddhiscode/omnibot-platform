package com.omnibot.rewards.dto;

public record BalanceResponse(
        int points,
        String tier,
        String tierDisplayName,
        double earnMultiplier,
        int lifetimePointsEarned,
        Integer nextTierThreshold,
        Integer pointsToNextTier,
        double redeemValueRupees
) {}
