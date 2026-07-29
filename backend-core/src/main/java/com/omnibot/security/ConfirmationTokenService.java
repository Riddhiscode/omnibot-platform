package com.omnibot.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfirmationTokenService {

    private static final long TTL_SECONDS = 300; // 5-minute window to confirm

    public enum ActionType {
        BOOK_RIDE, PLACE_FOOD_ORDER, PLACE_GROCERY_ORDER, PLACE_SHOPPING_ORDER, CANCEL_ORDER
    }

    /** Everything needed to execute the action later, captured at proposal time. */
    public record PendingAction(
            String userId,
            ActionType actionType,
            Map<String, String> args,
            String humanSummary,
            Instant expiresAt
    ) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final ConcurrentHashMap<String, PendingAction> pending = new ConcurrentHashMap<>();

    /** Called only by a write tool, when the LLM proposes a state-changing action. */
    public String issue(String userId, ActionType actionType, Map<String, String> args, String humanSummary) {
        String token = UUID.randomUUID().toString();
        pending.put(token, new PendingAction(
                userId, actionType, Map.copyOf(args), humanSummary,
                Instant.now().plusSeconds(TTL_SECONDS)
        ));
        return token;
    }

    /**
     * Called only by ConfirmationController, never by a tool. Atomic remove —
     * a second call with the same token, concurrent or not, always finds it gone.
     */
    public Optional<PendingAction> validateAndConsume(String token, String requestingUserId) {
        PendingAction action = pending.remove(token);

        if (action == null) return Optional.empty();               // unknown or already used
        if (action.isExpired()) return Optional.empty();            // expired
        if (!action.userId().equals(requestingUserId)) return Optional.empty(); // different user's session

        return Optional.of(action);
    }

    @Scheduled(fixedRate = 60_000)
    void purgeExpired() {
        pending.values().removeIf(PendingAction::isExpired);
    }
}
