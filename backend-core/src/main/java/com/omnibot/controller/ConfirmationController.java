package com.omnibot.controller;

import com.omnibot.security.ConfirmationTokenService;
import com.omnibot.security.ConfirmationTokenService.PendingAction;
import com.omnibot.agent.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/chat/confirm", "/v1/chat/confirm"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ConfirmationController {

    private final ConfirmationTokenService confirmationTokenService;
    private final VendorService vendorService;

    public ConfirmationController(ConfirmationTokenService confirmationTokenService, VendorService vendorService) {
        this.confirmationTokenService = confirmationTokenService;
        this.vendorService = vendorService;
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

        return ResponseEntity.ok(result);
    }
}
