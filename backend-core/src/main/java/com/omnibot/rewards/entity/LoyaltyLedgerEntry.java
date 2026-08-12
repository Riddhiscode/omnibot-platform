package com.omnibot.rewards.entity;

import com.omnibot.rewards.model.LedgerReason;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loyalty_ledger", indexes = {
        @Index(name = "idx_ledger_user_email", columnList = "user_email"),
        @Index(name = "idx_ledger_confirmation_token", columnList = "confirmation_token_id", unique = true)
})
public class LoyaltyLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "points_delta", nullable = false)
    private int pointsDelta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerReason reason;

    @Column(name = "vendor_id", length = 64)
    private String vendorId;

    @Column(name = "confirmation_token_id", length = 64, unique = true)
    private String confirmationTokenId;

    @Column(length = 512)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LoyaltyLedgerEntry() {}

    public LoyaltyLedgerEntry(String userEmail, int pointsDelta, LedgerReason reason,
                              String vendorId, String confirmationTokenId, String description) {
        this.userEmail = userEmail;
        this.pointsDelta = pointsDelta;
        this.reason = reason;
        this.vendorId = vendorId;
        this.confirmationTokenId = confirmationTokenId;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getPointsDelta() { return pointsDelta; }
    public void setPointsDelta(int pointsDelta) { this.pointsDelta = pointsDelta; }

    public LedgerReason getReason() { return reason; }
    public void setReason(LedgerReason reason) { this.reason = reason; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getConfirmationTokenId() { return confirmationTokenId; }
    public void setConfirmationTokenId(String confirmationTokenId) { this.confirmationTokenId = confirmationTokenId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
