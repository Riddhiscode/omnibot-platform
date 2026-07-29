package com.omnibot.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Issues and validates one-time confirmation tokens for the Zero-Trust
 * Payment Boundary.
 *
 * SECURITY DESIGN:
 * - Before any booking/order/buy action, the orchestrator calls issueToken().
 * - The token is shown to the user on the Execution Summary Card.
 * - The user clicks "Confirm" — the token is sent back with the request.
 * - The orchestrator calls validateAndConsume() before allowing the tool to execute.
 * - Tokens expire after 5 minutes and are single-use (consumed on first use).
 * - This means the LLM *cannot* bypass confirmation by calling the tool directly —
 *   it would need a valid, unexpired, unconsumed token which only the server can issue.
 */
@Component
public class ConfirmationTokenService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationTokenService.class);
    private static final long TOKEN_TTL_SECONDS = 300; // 5 minutes

    private record PendingConfirmation(
        String userId,
        String toolName,
        String contextSummary,
        Instant expiresAt
    ) {}

    private final Map<String, PendingConfirmation> pendingTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Issue a confirmation token for a pending action.
     * @param userId        the authenticated user ID
     * @param toolName      the tool that will be called on confirmation
     * @param summary       human-readable summary of what will be done
     * @return              a secure one-time token
     */
    public String issueToken(String userId, String toolName, String summary) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        pendingTokens.put(token, new PendingConfirmation(
            userId, toolName, summary,
            Instant.now().plusSeconds(TOKEN_TTL_SECONDS)
        ));

        // Clean up expired tokens while we're here
        purgeExpiredTokens();

        log.info("Confirmation token issued for user={} tool={}", userId, toolName);
        return token;
    }

    /**
     * Validate and consume a confirmation token.
     * Returns true only if the token exists, hasn't expired, belongs to the
     * correct user, and matches the tool being called.
     */
    public boolean validateAndConsume(String token, String userId, String toolName) {
        if (token == null || token.isBlank()) {
            log.warn("Confirmation attempted with null/blank token for tool={}", toolName);
            return false;
        }

        PendingConfirmation pending = pendingTokens.remove(token);

        if (pending == null) {
            log.warn("Confirmation token not found or already used: tool={} user={}", toolName, userId);
            return false;
        }

        if (Instant.now().isAfter(pending.expiresAt())) {
            log.warn("Confirmation token expired: tool={} user={}", toolName, userId);
            return false;
        }

        if (!pending.userId().equals(userId)) {
            log.error("Confirmation token user mismatch: expected={} got={}", pending.userId(), userId);
            return false;
        }

        if (!pending.toolName().equals(toolName)) {
            log.warn("Confirmation token tool mismatch: expected={} got={}", pending.toolName(), toolName);
            return false;
        }

        log.info("Confirmation token validated and consumed: tool={} user={}", toolName, userId);
        return true;
    }

    private void purgeExpiredTokens() {
        Instant now = Instant.now();
        pendingTokens.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}
