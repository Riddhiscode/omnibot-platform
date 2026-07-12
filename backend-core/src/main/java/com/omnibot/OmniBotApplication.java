package com.omnibot;

import com.omnibot.model.User;
import com.omnibot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class OmniBotApplication {

    private static final Logger log = LoggerFactory.getLogger(OmniBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OmniBotApplication.class, args);
        log.info("OmniBot Platform started successfully.");
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return email -> {
            User user = repo.findByEmail(email.toLowerCase())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name())
                    .accountLocked(!user.isActive())
                    .build();
        };
    }

    @Bean
    public CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            String adminEmail = "admin@omnibot.in";
            if (!repo.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .email(adminEmail)
                        .fullName("OmniBot Admin")
                        .passwordHash(encoder.encode("Admin@12345"))
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .isVerified(true)
                        .build();
                repo.save(admin);
                log.warn("Default admin seeded: {} — CHANGE PASSWORD IMMEDIATELY", adminEmail);
            }
        };
    }
}
