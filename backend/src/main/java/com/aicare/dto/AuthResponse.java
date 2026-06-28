package com.aicare.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String name, String email, String message) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String name;
        private String email;
        private String message;

        public Builder token(String token) { this.token = token; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, name, email, message);
        }
    }
}
