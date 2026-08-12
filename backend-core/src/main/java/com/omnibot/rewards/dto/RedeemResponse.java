package com.omnibot.rewards.dto;

public record RedeemResponse(
        int pointsRedeemed,
        double discountRupees,
        int remainingBalance
) {}
