package com.omnibot.config;

import com.omnibot.agent.MockServiceAdapter;
import com.omnibot.model.User;
import com.omnibot.model.VendorMapping;
import com.omnibot.repository.UserRepository;
import com.omnibot.repository.VendorMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the database with a demo user on every clean startup.
 * Demo credentials:
 *   Email:    demo@omnibot.in
 *   Password: Demo@1234
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedDemoUser(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   VendorMappingRepository vendorMappingRepository,
                                   com.omnibot.repository.ConnectedAccountRepository connectedAccountRepository) {
        return args -> {
            seedDemoUser(userRepository, passwordEncoder, connectedAccountRepository);
            seedVendors(vendorMappingRepository);
        };
    }

    private void seedDemoUser(UserRepository userRepository, PasswordEncoder passwordEncoder, com.omnibot.repository.ConnectedAccountRepository connectedAccountRepository) {
        String demoEmail = "demo@omnibot.in";
        User demo;
        if (userRepository.findByEmail(demoEmail).isEmpty()) {
            demo = User.builder()
                    .email(demoEmail)
                    .fullName("OmniBot Demo User")
                    .passwordHash(passwordEncoder.encode("Demo@1234"))
                    .phone("9876543210")
                    .role(User.Role.USER)
                    .isActive(true)
                    .isVerified(true)
                    .build();
            userRepository.save(demo);
            log.info("✅ Demo user seeded: {} / Demo@1234", demoEmail);
        } else {
            demo = userRepository.findByEmail(demoEmail).get();
            log.info("ℹ️  Demo user already exists, skipping seed.");
        }

        String adminEmail = "admin@omnibot.in";
        User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            return User.builder()
                    .email(adminEmail)
                    .fullName("angel")
                    .phone("9999999999")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .build();
        });
        admin.setFullName("angel");
        admin.setPasswordHash(passwordEncoder.encode("Admin@12345"));
        userRepository.save(admin);
        log.info("✅ Admin user seeded/updated: {} / Admin@12345", adminEmail);
        
        // Seed Connected Accounts if they don't exist
        if (connectedAccountRepository.findByUserId(demo.getId()).isEmpty()) {
            connectedAccountRepository.save(new com.omnibot.model.ConnectedAccount(demo.getId(), "zomato", com.omnibot.model.AccountStatus.CONNECTED, 24));
            connectedAccountRepository.save(new com.omnibot.model.ConnectedAccount(demo.getId(), "uber", com.omnibot.model.AccountStatus.CONNECTED, 41));
            connectedAccountRepository.save(new com.omnibot.model.ConnectedAccount(demo.getId(), "amazon", com.omnibot.model.AccountStatus.CONNECTED, 32));
            log.info("✅ Default connected accounts seeded.");
        }
    }

    private void seedVendors(VendorMappingRepository vendorMappingRepository) {
        seedVendorCategory(vendorMappingRepository, "FOOD", MockServiceAdapter.FOOD_VENDORS);
        seedVendorCategory(vendorMappingRepository, "GROCERY", MockServiceAdapter.GROCERY_VENDORS);
        seedVendorCategory(vendorMappingRepository, "TRANSPORT", MockServiceAdapter.TRANSPORT_VENDORS);
        seedVendorCategory(vendorMappingRepository, "SHOPPING", MockServiceAdapter.SHOPPING_VENDORS);
    }

    private void seedVendorCategory(VendorMappingRepository repo, String category, java.util.List<String> vendors) {
        for (String name : vendors) {
            String vendorId = name.toUpperCase().replace("-", "").replace(" ", "");
            if (repo.findByVendorId(vendorId).isEmpty()) {
                VendorMapping mapping = new VendorMapping();
                mapping.setVendorId(vendorId);
                mapping.setVendorName(name);
                mapping.setCategory(category);
                mapping.setApiEndpoint("mock://" + vendorId.toLowerCase());
                mapping.setApiKeyAlias("MOCK_" + vendorId);
                mapping.setIsActive(true);
                repo.save(mapping);
            }
        }
        log.info("✅ Vendor catalog seeded for category {}", category);
    }
}
