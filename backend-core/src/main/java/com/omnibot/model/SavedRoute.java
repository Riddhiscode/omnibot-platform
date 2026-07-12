package com.omnibot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A saved route (source -> destination) for quick future booking,
 * with optional preferred transport app.
 */
@Entity
@Table(name = "saved_routes")
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String source;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(name = "preferred_app", length = 30)
    private String preferredApp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getSource() { return source; }
    public void setSource(String v) { this.source = v; }
    public String getDestination() { return destination; }
    public void setDestination(String v) { this.destination = v; }
    public String getPreferredApp() { return preferredApp; }
    public void setPreferredApp(String v) { this.preferredApp = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
