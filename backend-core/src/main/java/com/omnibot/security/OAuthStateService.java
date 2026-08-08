package com.omnibot.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, single-use store binding OAuth state to PKCE verifier and requesting user ID.
 */
@Service
public class OAuthStateService {

    private static final long TTL_SECONDS = 600; // 10 minutes

    public record OAuthStateData(Long userId, String codeVerifier, Instant createdAt) {}

    private final Map<String, OAuthStateData> stateStore = new ConcurrentHashMap<>();

    public String generateAndStoreState(Long userId, String codeVerifier) {
        String state = UUID.randomUUID().toString();
        stateStore.put(state, new OAuthStateData(userId, codeVerifier, Instant.now()));
        return state;
    }

    public OAuthStateData validateAndConsume(String state) {
        if (state == null || state.isBlank()) return null;
        OAuthStateData data = stateStore.remove(state);
        if (data == null) return null;
        if (Instant.now().isAfter(data.createdAt().plusSeconds(TTL_SECONDS))) {
            return null; // Expired
        }
        return data;
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpired() {
        Instant cutoff = Instant.now().minusSeconds(TTL_SECONDS);
        stateStore.entrySet().removeIf(entry -> entry.getValue().createdAt().isBefore(cutoff));
    }
}
