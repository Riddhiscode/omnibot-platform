package com.omnibot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class ChatDto {

    private ChatDto() {}

    public static class ChatRequest {
        @NotBlank(message = "Message cannot be empty")
        @Size(max = 1000, message = "Message too long")
        private String message;

        private String sessionId;

        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String v) { this.sessionId = v; }
    }

    public static class ChatResponse {
        private String sessionId;
        private String reply;
        private String intent;
        private List<ServiceCard> services;
        private LocalDateTime timestamp;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String v) { this.sessionId = v; }
        public String getReply() { return reply; }
        public void setReply(String v) { this.reply = v; }
        public String getIntent() { return intent; }
        public void setIntent(String v) { this.intent = v; }
        public List<ServiceCard> getServices() { return services; }
        public void setServices(List<ServiceCard> v) { this.services = v; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime v) { this.timestamp = v; }
    }

    public static class ServiceCard {
        private String name;
        private String logo;
        private String action;
        private String estimate;
        private String price;
        private String rating;

        public ServiceCard(String name, String logo, String action,
                           String estimate, String price, String rating) {
            this.name = name; this.logo = logo; this.action = action;
            this.estimate = estimate; this.price = price; this.rating = rating;
        }

        public String getName() { return name; }
        public String getLogo() { return logo; }
        public String getAction() { return action; }
        public String getEstimate() { return estimate; }
        public String getPrice() { return price; }
        public String getRating() { return rating; }
    }

    public static class ChatHistoryItem {
        private String role;
        private String content;
        private String intent;
        private LocalDateTime timestamp;

        public ChatHistoryItem(String role, String content,
                               String intent, LocalDateTime timestamp) {
            this.role = role; this.content = content;
            this.intent = intent; this.timestamp = timestamp;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public String getIntent() { return intent; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
