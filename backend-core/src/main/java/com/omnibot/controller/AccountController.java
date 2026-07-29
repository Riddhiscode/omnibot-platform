package com.omnibot.controller;

import com.omnibot.model.AccountStatus;
import com.omnibot.model.ConnectedAccount;
import com.omnibot.model.User;
import com.omnibot.repository.ConnectedAccountRepository;
import com.omnibot.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/accounts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080"})
public class AccountController {

    private final ConnectedAccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountController(ConnectedAccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConnectedAccount>> getUserAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(accountRepository.findByUserId(userId));
    }

    @PostMapping("/link")
    public ResponseEntity<ConnectedAccount> linkAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        
        Long userId = resolveUserId(userDetails);
        String vendorId = payload.get("vendorId");

        if (vendorId == null || vendorId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if already linked
        Optional<ConnectedAccount> existing = accountRepository.findByUserIdAndVendorId(userId, vendorId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get()); // Return existing instead of throwing error
        }

        ConnectedAccount newAccount = new ConnectedAccount(userId, vendorId, AccountStatus.CONNECTED, 0);
        return ResponseEntity.ok(accountRepository.save(newAccount));
    }

    @DeleteMapping("/unlink/{vendorId}")
    @Transactional
    public ResponseEntity<Void> unlinkAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String vendorId) {
        
        Long userId = resolveUserId(userDetails);
        accountRepository.deleteByUserIdAndVendorId(userId, vendorId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return 1L;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(1L);
    }
}
