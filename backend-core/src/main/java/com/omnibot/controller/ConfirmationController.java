package com.omnibot.controller;

import com.omnibot.agent.VendorService;
import com.omnibot.agent.dto.BookingConfirmation;
import com.omnibot.agent.dto.OrderConfirmation;
import com.omnibot.rewards.service.LoyaltyService;
import com.omnibot.security.ConfirmationTokenService;
import com.omnibot.security.ConfirmationTokenService.ActionType;
import com.omnibot.security.ConfirmationTokenService.PendingAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/chat/confirm", "/v1/chat/confirm"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ConfirmationController {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationController.class);

    private final ConfirmationTokenService confirmationTokenService;
    private final VendorService vendorService;
    private final LoyaltyService loyaltyService;

    public ConfirmationController(ConfirmationTokenService confirmationTokenService,
                                  VendorService vendorService,
                                  LoyaltyService loyaltyService) {
        this.confirmationTokenService = confirmationTokenService;
        this.vendorService = vendorService;
        this.loyaltyService = loyaltyService;
    }

    // No request body — the token is the only input, and it already carries
    // exactly what was proposed.
    @PostMapping("/{token}")
    public ResponseEntity<?> confirm(@PathVariable String token, Authentication authentication) {
        String userId = (authentication != null && authentication.getName() != null && !"anonymousUser".equals(authentication.getName()))
                ? authentication.getName()
                : "admin@omnibot.in";

        Optional<PendingAction> maybeAction = confirmationTokenService.validateAndConsume(token, userId);
        if (maybeAction.isEmpty()) {
            return ResponseEntity.status(410)
                    .body(Map.of("error", "This confirmation has expired or was already used — please ask again."));
        }

        PendingAction action = maybeAction.get();
        Map<String, String> a = action.args();

        Object result = switch (action.actionType()) {
            case BOOK_RIDE -> vendorService.bookRide(a.get("vendorId"), a.get("pickup"), a.get("destination"), a.get("requestedTime"));
            case PLACE_FOOD_ORDER -> vendorService.placeFoodOrder(a.get("vendorId"), a.get("items"), a.get("deliveryAddress"), a.get("deliveryTime"));
            case PLACE_GROCERY_ORDER -> vendorService.placeGroceryOrder(a.get("vendorId"), a.get("items"), a.get("deliveryAddress"));
            case PLACE_SHOPPING_ORDER -> vendorService.placeShoppingOrder(a.get("vendorId"), a.get("productQuery"), a.get("shippingAddress"));
            case CANCEL_ORDER -> vendorService.cancelOrder(a.get("orderId"));
        };

        maybeEarnPoints(userId, token, action, result, a.get("vendorId"));

        return ResponseEntity.ok(result);
    }

    private void maybeEarnPoints(String userEmail, String confirmationTokenId, PendingAction action,
                                 Object result, String vendorId) {
        if (action.actionType() == ActionType.CANCEL_ORDER) return;

        BigDecimal orderTotal = extractOrderTotal(result);
        if (orderTotal == null || vendorId == null || vendorId.isBlank()) return;

        try {
            loyaltyService.earnPointsForOrder(userEmail, orderTotal, vendorId, confirmationTokenId);
        } catch (Exception e) {
            log.warn("Rewards earn failed for {} — order still confirmed", userEmail, e);
        }
    }

    private BigDecimal extractOrderTotal(Object result) {
        if (result instanceof OrderConfirmation oc) {
            return parseRupees(oc.getTotalAmount());
        }
        if (result instanceof BookingConfirmation bc) {
            return parseRupees(bc.getPrice());
        }
        return null;
    }

    private BigDecimal parseRupees(String amount) {
        if (amount == null || amount.isBlank()) return null;
        String digits = amount.replaceAll("[^0-9.]", "");
        if (digits.isBlank()) return null;
        return new BigDecimal(digits);
    }
}
