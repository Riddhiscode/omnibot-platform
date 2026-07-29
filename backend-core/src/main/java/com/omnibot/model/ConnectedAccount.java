package com.omnibot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "connected_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vendor_id"})
})
public class ConnectedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vendor_id", nullable = false)
    private String vendorId; // e.g., "zomato", "uber"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private Integer orderCount = 0;

    public ConnectedAccount() {
    }

    public ConnectedAccount(Long userId, String vendorId, AccountStatus status, Integer orderCount) {
        this.userId = userId;
        this.vendorId = vendorId;
        this.status = status;
        this.orderCount = orderCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
}
