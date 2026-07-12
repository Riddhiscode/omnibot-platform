package com.omnibot.repository;

import com.omnibot.model.VendorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorMappingRepository extends JpaRepository<VendorMapping, Long> {
    Optional<VendorMapping> findByVendorId(String vendorId);
}
