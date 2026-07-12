package com.omnibot.model;

import jakarta.validation.constraints.*;

public final class AuthDto {

    private AuthDto() {}

    // ---- REQUEST DTOs ----

    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100)
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 180)
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64)
        private String password;

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        private String phone;

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
        public void setFullName(String v) { this.fullName = v; }
        public void setEmail(String v) { this.email = v; }
        public void setPassword(String v) { this.password = v; }
        public void setPhone(String v) { this.phone = v; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 180)
        private String email;

        @NotBlank
        @Size(min = 8, max = 64)
        private String password;

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setEmail(String v) { this.email = v; }
        public void setPassword(String v) { this.password = v; }
    }

    public static class ForgotPasswordRequest {
        @NotBlank @Email @Size(max = 180)
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
    }

    // ---- RESPONSE DTOs ----

    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserProfile user;

        private AuthResponse() {}

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType() { return tokenType; }
        public long getExpiresIn() { return expiresIn; }
        public UserProfile getUser() { return user; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AuthResponse r = new AuthResponse();
            public Builder accessToken(String v) { r.accessToken = v; return this; }
            public Builder refreshToken(String v) { r.refreshToken = v; return this; }
            public Builder tokenType(String v) { r.tokenType = v; return this; }
            public Builder expiresIn(long v) { r.expiresIn = v; return this; }
            public Builder user(UserProfile v) { r.user = v; return this; }
            public AuthResponse build() { return r; }
        }
    }

    public static class UserProfile {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private boolean isVerified;
        private String role;

        private UserProfile() {}

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public boolean isVerified() { return isVerified; }
        public String getRole() { return role; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final UserProfile p = new UserProfile();
            public Builder id(Long v) { p.id = v; return this; }
            public Builder fullName(String v) { p.fullName = v; return this; }
            public Builder email(String v) { p.email = v; return this; }
            public Builder phone(String v) { p.phone = v; return this; }
            public Builder isVerified(boolean v) { p.isVerified = v; return this; }
            public Builder role(String v) { p.role = v; return this; }
            public UserProfile build() { return p; }
        }
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        private ApiResponse() {}

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }

        public static ApiResponse ok(String message) {
            ApiResponse r = new ApiResponse();
            r.success = true; r.message = message; return r;
        }
        public static ApiResponse ok(String message, Object data) {
            ApiResponse r = new ApiResponse();
            r.success = true; r.message = message; r.data = data; return r;
        }
        public static ApiResponse error(String message) {
            ApiResponse r = new ApiResponse();
            r.success = false; r.message = message; return r;
        }
    }
}
