package com.omnibot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks an in-progress multi-step conversation (slot-filling flow).
 * Example: Booking a cab requires source, destination, time, etc.
 * collected across multiple chat turns before showing service cards.
 */
@Entity
@Table(name = "conversation_states")
public class ConversationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false, length = 20)
    private FlowType flowType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 20)
    private Step currentStep;

    // JSON-encoded collected answers: {"source":"...","destination":"...", ...}
    @Column(columnDefinition = "TEXT")
    private String data;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FlowType { TRANSPORT_BOOK, FOOD_ORDER, SHOPPING_ORDER }

    public enum Step {
        ASK_SOURCE, ASK_DESTINATION, ASK_TIME,
        ASK_SAVE_ROUTE, ASK_PREFERENCE, DONE
    }

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { this.sessionId = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public FlowType getFlowType() { return flowType; }
    public void setFlowType(FlowType v) { this.flowType = v; }
    public Step getCurrentStep() { return currentStep; }
    public void setCurrentStep(Step v) { this.currentStep = v; }
    public String getData() { return data; }
    public void setData(String v) { this.data = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
