package com.omnibot.service;

import com.omnibot.model.AuthDto.*;
import com.omnibot.model.User;
import com.omnibot.repository.UserRepository;
import com.omnibot.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ApiResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Registration failed. Please check your details and try again.");
        }

        User user = User.builder()
                .email(email)
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .isActive(true)
                .isVerified(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", email);

        return ApiResponse.ok(
            "Account created! Please verify your email to activate all features.",
            UserProfile.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isVerified(false)
                .role(user.getRole().name())
                .build()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (!user.isActive()) {
                throw new DisabledException("Account is deactivated. Please contact support.");
            }

            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

            String accessToken  = jwtUtil.generateAccessToken(email, user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(email);

            log.info("User logged in: {}", email);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
                    .user(UserProfile.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .phone(user.getPhone())
                            .isVerified(user.isVerified())
                            .role(user.getRole().name())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", email);
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Password reset requested for: {}", email);
        return ApiResponse.ok("If an account exists for that email, a reset link has been sent.");
    }
}
