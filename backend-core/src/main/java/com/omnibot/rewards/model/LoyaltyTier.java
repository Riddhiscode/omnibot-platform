package com.omnibot.rewards.model;

public enum LoyaltyTier {

    MEMBER(0, 1.0, "Member"),
    SILVER(500, 1.25, "Silver"),
    GOLD(2000, 1.5, "Gold"),
    PLATINUM(5000, 2.0, "Platinum");

    private final int lifetimePointsThreshold;
    private final double earnMultiplier;
    private final String displayName;

    LoyaltyTier(int lifetimePointsThreshold, double earnMultiplier, String displayName) {
        this.lifetimePointsThreshold = lifetimePointsThreshold;
        this.earnMultiplier = earnMultiplier;
        this.displayName = displayName;
    }

    public int getLifetimePointsThreshold() { return lifetimePointsThreshold; }
    public double getEarnMultiplier() { return earnMultiplier; }
    public String getDisplayName() { return displayName; }

    public static LoyaltyTier fromLifetimePoints(int lifetimePoints) {
        LoyaltyTier current = MEMBER;
        for (LoyaltyTier tier : values()) {
            if (lifetimePoints >= tier.lifetimePointsThreshold) {
                current = tier;
            }
        }
        return current;
    }

    public LoyaltyTier nextTier() {
        return switch (this) {
            case MEMBER -> SILVER;
            case SILVER -> GOLD;
            case GOLD -> PLATINUM;
            case PLATINUM -> null;
        };
    }
}
