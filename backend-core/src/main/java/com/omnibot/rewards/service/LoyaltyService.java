package com.omnibot.rewards.service;

import com.omnibot.rewards.config.RewardsProperties;
import com.omnibot.rewards.dto.BalanceResponse;
import com.omnibot.rewards.dto.LedgerEntryResponse;
import com.omnibot.rewards.dto.RedeemResponse;
import com.omnibot.rewards.entity.LoyaltyAccount;
import com.omnibot.rewards.entity.LoyaltyLedgerEntry;
import com.omnibot.rewards.model.LedgerReason;
import com.omnibot.rewards.model.LoyaltyTier;
import com.omnibot.rewards.repository.LoyaltyAccountRepository;
import com.omnibot.rewards.repository.LoyaltyLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoyaltyService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyLedgerRepository ledgerRepository;
    private final RewardsProperties properties;

    public LoyaltyService(LoyaltyAccountRepository accountRepository,
                          LoyaltyLedgerRepository ledgerRepository,
                          RewardsProperties properties) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.properties = properties;
    }

    /**
     * Awards points for a confirmed order. Runs in its own transaction so a rewards
     * failure never rolls back the underlying order. Idempotent on confirmationTokenId.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void earnPointsForOrder(String userEmail, BigDecimal orderTotal, String vendorId,
                                   String confirmationTokenId) {
        if (userEmail == null || userEmail.isBlank()) return;
        if (orderTotal == null || orderTotal.signum() <= 0) return;
        if (confirmationTokenId != null && ledgerRepository.existsByConfirmationTokenId(confirmationTokenId)) {
            log.debug("Skipping duplicate earn for token {}", confirmationTokenId);
            return;
        }

        LoyaltyAccount account = accountRepository.findWithLockByUserEmail(userEmail)
                .orElseGet(() -> accountRepository.save(new LoyaltyAccount(userEmail)));

        LoyaltyTier tier = LoyaltyTier.fromLifetimePoints(account.getLifetimePointsEarned());
        int basePoints = orderTotal.divide(BigDecimal.valueOf(properties.getRupeesPerPoint()), 0, RoundingMode.DOWN).intValue();
        int earned = (int) Math.floor(basePoints * tier.getEarnMultiplier());
        if (earned <= 0) return;

        account.setPointsBalance(account.getPointsBalance() + earned);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + earned);
        account.setTier(LoyaltyTier.fromLifetimePoints(account.getLifetimePointsEarned()));
        accountRepository.save(account);

        String description = "Earned from %s order (₹%s)".formatted(
                vendorId != null ? vendorId : "vendor",
                orderTotal.setScale(0, RoundingMode.HALF_UP));
        ledgerRepository.save(new LoyaltyLedgerEntry(
                userEmail, earned, LedgerReason.EARN, vendorId, confirmationTokenId, description));

        log.info("Awarded {} points to {} for {} order", earned, userEmail, vendorId);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String userEmail) {
        LoyaltyAccount account = accountRepository.findByUserEmail(userEmail)
                .orElseGet(() -> new LoyaltyAccount(userEmail));

        LoyaltyTier tier = account.getTier() != null
                ? account.getTier()
                : LoyaltyTier.fromLifetimePoints(account.getLifetimePointsEarned());
        LoyaltyTier next = tier.nextTier();

        Integer nextThreshold = next != null ? next.getLifetimePointsThreshold() : null;
        Integer pointsToNext = nextThreshold != null
                ? Math.max(0, nextThreshold - account.getLifetimePointsEarned())
                : null;

        double redeemValue = account.getPointsBalance() * properties.getRedeemRateRupees();

        return new BalanceResponse(
                account.getPointsBalance(),
                tier.name(),
                tier.getDisplayName(),
                tier.getEarnMultiplier(),
                account.getLifetimePointsEarned(),
                nextThreshold,
                pointsToNext,
                redeemValue
        );
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getHistory(String userEmail, int limit) {
        int capped = Math.min(Math.max(limit, 1), 100);
        return ledgerRepository.findByUserEmailOrderByCreatedAtDesc(userEmail, PageRequest.of(0, capped))
                .stream()
                .map(e -> new LedgerEntryResponse(
                        e.getId(),
                        e.getPointsDelta(),
                        e.getReason(),
                        e.getVendorId(),
                        e.getDescription(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public RedeemResponse redeem(String userEmail, int points) {
        if (points < properties.getMinRedemptionPoints()) {
            throw new IllegalArgumentException(
                    "Minimum redemption is " + properties.getMinRedemptionPoints() + " points");
        }

        LoyaltyAccount account = accountRepository.findWithLockByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("No loyalty account for " + userEmail));

        if (account.getPointsBalance() < points) {
            throw new IllegalArgumentException("Insufficient points balance");
        }

        account.setPointsBalance(account.getPointsBalance() - points);
        accountRepository.save(account);

        double discountRupees = points * properties.getRedeemRateRupees();
        ledgerRepository.save(new LoyaltyLedgerEntry(
                userEmail,
                -points,
                LedgerReason.REDEEM,
                null,
                null,
                "Redeemed for ₹%.2f discount".formatted(discountRupees)
        ));

        log.info("Redeemed {} points for {} (₹{})", points, userEmail, discountRupees);
        return new RedeemResponse(points, discountRupees, account.getPointsBalance());
    }
}
