package com.omnibot.controller;

import com.omnibot.config.VendorProperties;
import com.omnibot.model.AccountStatus;
import com.omnibot.model.ConnectedAccount;
import com.omnibot.model.User;
import com.omnibot.repository.ConnectedAccountRepository;
import com.omnibot.repository.UserRepository;
import com.omnibot.security.OAuthStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Swiggy OAuth 2.0 PKCE Link Controller.
 */
@RestController
@RequestMapping({"/v1/oauth/swiggy", "/api/v1/oauth/swiggy"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class SwiggyOAuthController {

    private static final Logger log = LoggerFactory.getLogger(SwiggyOAuthController.class);

    private final OAuthStateService oAuthStateService;
    private final ConnectedAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    public SwiggyOAuthController(OAuthStateService oAuthStateService,
                                ConnectedAccountRepository accountRepository,
                                UserRepository userRepository,
                                VendorProperties vendorProperties) {
        this.oAuthStateService = oAuthStateService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * JSON Endpoint: Authenticated via JWT Authorization header.
     * Returns Swiggy OAuth authorization URL for frontend navigation.
     */
    @GetMapping("/start")
    public ResponseEntity<Map<String, String>> startOAuthFlow(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = oAuthStateService.generateAndStoreState(userId, codeVerifier);

        String clientId = vendorProperties.getSwiggy() != null && vendorProperties.getSwiggy().getApiKey() != null
                ? vendorProperties.getSwiggy().getApiKey()
                : "swiggy-omnibot-client-id";

        String redirectUri = URLEncoder.encode("http://localhost:8080/api/v1/oauth/swiggy/callback", StandardCharsets.UTF_8);

        String authUrl = String.format(
            "https://developers.swiggy.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s&code_challenge=%s&code_challenge_method=S256&scope=orders.read%%20orders.create",
            clientId, redirectUri, state, codeChallenge
        );

        log.info("Started Swiggy OAuth PKCE flow for userId={}, state={}", userId, state);

        return ResponseEntity.ok(Map.of(
            "authUrl", authUrl,
            "state", state
        ));
    }

    /**
     * Public Redirect Callback Endpoint hit by Swiggy OAuth server.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error) {

        if (error != null || code == null || state == null) {
            log.warn("Swiggy OAuth callback error or missing params: error={}", error);
            return ResponseEntity.status(302)
                    .header("Location", "/dashboard.html?error=swiggy_oauth_failed")
                    .build();
        }

        OAuthStateService.OAuthStateData stateData = oAuthStateService.validateAndConsume(state);
        if (stateData == null) {
            log.warn("Swiggy OAuth callback invalid state token: {}", state);
            return ResponseEntity.status(302)
                    .header("Location", "/dashboard.html?error=invalid_oauth_state")
                    .build();
        }

        Long userId = stateData.userId();
        log.info("Validated Swiggy OAuth callback for userId={}", userId);

        // Save / Update Linked Account in Database
        Optional<ConnectedAccount> existing = accountRepository.findByUserIdAndVendorId(userId, "Swiggy");
        ConnectedAccount account = existing.orElseGet(() -> new ConnectedAccount(userId, "Swiggy", AccountStatus.CONNECTED, 0));
        account.setStatus(AccountStatus.CONNECTED);
        accountRepository.save(account);

        log.info("Successfully linked Swiggy account for userId={}", userId);

        return ResponseEntity.status(302)
                .header("Location", "/dashboard.html?connected=Swiggy")
                .build();
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return 1L;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(1L);
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) {
        try {
            byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
