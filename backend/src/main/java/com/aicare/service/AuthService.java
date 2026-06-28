package com.aicare.service;

import com.aicare.dto.*;
import com.aicare.model.User;
import com.aicare.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(user);

        String fullName = user.getFirstName() + " " + user.getLastName();
        String token = jwtService.generateToken(user.getEmail(), Map.of("name", fullName));

        return AuthResponse.builder()
                .token(token)
                .name(fullName)
                .email(user.getEmail())
                .message("Account created successfully!")
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        String fullName = user.getFirstName() + " " + user.getLastName();
        String token = jwtService.generateToken(user.getEmail(), Map.of("name", fullName));

        return AuthResponse.builder()
                .token(token)
                .name(fullName)
                .email(user.getEmail())
                .message("Logged in successfully!")
                .build();
    }
}
