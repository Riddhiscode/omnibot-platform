package com.omnibot.rewards.entity;

import com.omnibot.rewards.model.LoyaltyTier;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loyalty_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_email")
})
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "points_balance", nullable = false)
    private int pointsBalance = 0;

    @Column(name = "lifetime_points_earned", nullable = false)
    private int lifetimePointsEarned = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoyaltyTier tier = LoyaltyTier.MEMBER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LoyaltyAccount() {}

    public LoyaltyAccount(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public int getLifetimePointsEarned() { return lifetimePointsEarned; }
    public void setLifetimePointsEarned(int lifetimePointsEarned) { this.lifetimePointsEarned = lifetimePointsEarned; }

    public LoyaltyTier getTier() { return tier; }
    public void setTier(LoyaltyTier tier) { this.tier = tier; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
