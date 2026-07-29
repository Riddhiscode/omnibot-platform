package com.omnibot.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Issues and validates action-bound, single-use confirmation tokens for the Zero-Trust
 * Payment Boundary.
 *
 * SECURITY DESIGN:
 * - Tokens are server-generated only (never accepted from LLM output).
 * - Bound to (userId + toolName + argsHash).
 * - Single-use (consumed on first validation attempt).
 * - 300 second (5 min) TTL.
 * - Triggered via explicit UI user confirmation endpoints.
 */
@Component
public class ConfirmationTokenService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationTokenService.class);
    private static final long TOKEN_TTL_SECONDS = 300; // 5 minutes

    private record PendingConfirmation(
        String userId,
        String toolName,
        String argsHash,
        String contextSummary,
        Instant expiresAt
    ) {}

    private final Map<String, PendingConfirmation> pendingTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Issue a confirmation token bound to a specific user, tool, and parameter fingerprint.
     */
    public String issueToken(String userId, String toolName, String rawArgs, String summary) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String argsHash = hashArgs(rawArgs);

        pendingTokens.put(token, new PendingConfirmation(
            userId, toolName, argsHash, summary,
            Instant.now().plusSeconds(TOKEN_TTL_SECONDS)
        ));

        purgeExpiredTokens();
        log.info("Action-bound confirmation token issued: user={}, tool={}, hash={}", userId, toolName, argsHash);
        return token;
    }

    /**
     * Validate and consume a confirmation token.
     */
    public boolean validateAndConsume(String token, String userId, String toolName, String rawArgs) {
        if (token == null || token.isBlank()) {
            log.warn("Confirmation failed: missing token for tool={}", toolName);
            return false;
        }

        PendingConfirmation pending = pendingTokens.remove(token);

        if (pending == null) {
            log.warn("Confirmation failed: token not found or already consumed (tool={}, user={})", toolName, userId);
            return false;
        }

        if (Instant.now().isAfter(pending.expiresAt())) {
            log.warn("Confirmation failed: token expired (tool={}, user={})", toolName, userId);
            return false;
        }

        if (!pending.userId().equals(userId)) {
            log.error("Confirmation failed: user mismatch (expected={}, got={})", pending.userId(), userId);
            return false;
        }

        if (!pending.toolName().equalsIgnoreCase(toolName)) {
            log.warn("Confirmation failed: tool mismatch (expected={}, got={})", pending.toolName(), toolName);
            return false;
        }

        String providedHash = hashArgs(rawArgs);
        if (!pending.argsHash().equals(providedHash)) {
            log.error("Confirmation failed: argument fingerprint mismatch! (expected={}, got={})", pending.argsHash(), providedHash);
            return false;
        }

        log.info("Action-bound confirmation token validated and consumed: tool={}, user={}", toolName, userId);
        return true;
    }

    private String hashArgs(String args) {
        if (args == null) args = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(args.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            return String.valueOf(args.hashCode());
        }
    }

    private void purgeExpiredTokens() {
        Instant now = Instant.now();
        pendingTokens.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}
