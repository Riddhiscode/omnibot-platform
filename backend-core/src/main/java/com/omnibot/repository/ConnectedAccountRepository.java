package com.omnibot.repository;

import com.omnibot.model.ConnectedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectedAccountRepository extends JpaRepository<ConnectedAccount, Long> {
    List<ConnectedAccount> findByUserId(Long userId);
    Optional<ConnectedAccount> findByUserIdAndVendorId(Long userId, String vendorId);
    void deleteByUserIdAndVendorId(Long userId, String vendorId);
}
