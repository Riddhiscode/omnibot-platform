package com.omnibot.rewards.controller;

import com.omnibot.rewards.dto.BalanceResponse;
import com.omnibot.rewards.dto.LedgerEntryResponse;
import com.omnibot.rewards.dto.RedeemRequest;
import com.omnibot.rewards.dto.RedeemResponse;
import com.omnibot.rewards.service.LoyaltyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/rewards", "/v1/rewards"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class RewardsController {

    private final LoyaltyService loyaltyService;

    public RewardsController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> balance(Authentication authentication) {
        String email = resolveEmail(authentication);
        return ResponseEntity.ok(loyaltyService.getBalance(email));
    }

    @GetMapping("/history")
    public ResponseEntity<List<LedgerEntryResponse>> history(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        String email = resolveEmail(authentication);
        return ResponseEntity.ok(loyaltyService.getHistory(email, limit));
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(
            Authentication authentication,
            @Valid @RequestBody RedeemRequest request) {
        String email = resolveEmail(authentication);
        try {
            RedeemResponse response = loyaltyService.redeem(email, request.points());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String resolveEmail(Authentication authentication) {
        if (authentication != null && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "admin@omnibot.in";
    }
}
