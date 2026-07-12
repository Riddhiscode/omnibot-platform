package com.omnibot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A saved delivery address with optional preferred food app,
 * for faster repeat food/grocery orders.
 */
@Entity
@Table(name = "saved_addresses")
public class SavedAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "preferred_app", length = 30)
    private String preferredApp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getPreferredApp() { return preferredApp; }
    public void setPreferredApp(String v) { this.preferredApp = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
