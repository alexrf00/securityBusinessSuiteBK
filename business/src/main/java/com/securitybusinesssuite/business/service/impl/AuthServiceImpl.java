// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/AuthServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.exception.AuthenticationException;
import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.exception.UserAlreadyExistsException;
import com.securitybusinesssuite.business.service.AuthService;
import com.securitybusinesssuite.business.service.EmailService;
import com.securitybusinesssuite.business.service.JwtService;
import com.securitybusinesssuite.data.entity.User;
import com.securitybusinesssuite.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // If email is already verified, user is fully registered
            if (user.isEmailVerified()) {
                throw new UserAlreadyExistsException("Email already registered and verified");
            }

            // If email not verified, resend verification email
            if (!user.isEmailVerified()) {
                // Generate new verification token
                String newVerificationToken = UUID.randomUUID().toString();
                user.setEmailVerificationToken(newVerificationToken);
                user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                User updatedUser = userRepository.update(user);

                // Resend verification email
                emailService.sendVerificationEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        newVerificationToken
                );

                return AuthResponse.builder().message("Verification email resent. Please check your email.")
                        .user(UserDto.fromEntity(updatedUser))
                        .requiresEmailVerification(true)
                        .build();
            }
        }

        // Create new user
        String verificationToken = UUID.randomUUID().toString();
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerified(false)
                .provider(User.AuthProvider.LOCAL)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                verificationToken
        );

        return AuthResponse.builder()
                .message("Registration successful. Please check your email to verify your account.")
                .user(UserDto.fromEntity(savedUser))
                .requiresEmailVerification(true)
                .build();
    }

    @Override
    public TokenPair login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Check if OAuth user trying to login with password
        if (user.getProvider() != User.AuthProvider.LOCAL) {
            throw new AuthenticationException("Please login with " + user.getProvider());
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Check email verification
        if (!user.isEmailVerified()) {
            throw new AuthenticationException("Please verify your email before logging in");
        }

        return jwtService.generateTokenPair(user.getId(), user.getEmail());
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Verification token has expired");
        }

        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.update(user);

        log.info("Email verified for user: {}", user.getEmail());
        return user.getEmail();
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String tokenType = jwtService.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new AuthenticationException("Invalid token type");
        }

        UUID userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return jwtService.generateTokenPair(user.getId(), user.getEmail());
    }

    @Override
    @Transactional
    public OAuthResult processOAuthLogin(String email, String provider, String providerId, Map<String, Object> attributes) {
        User.AuthProvider authProvider = User.AuthProvider.valueOf(provider.toUpperCase());

        // Check if OAuth user exists
        Optional<User> existingOAuthUser = userRepository.findByProviderAndProviderId(authProvider, providerId);
        if (existingOAuthUser.isPresent()) {
            log.info("OAuth user found, logging in: {}", email);
            return new OAuthResult(existingOAuthUser.get(), false); // Existing user
        }

        // Check if email exists (link accounts)
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();

            // Update to OAuth provider
            user.setProvider(authProvider);
            user.setProviderId(providerId);
            user.setEmailVerified(true);

            // Update name if not set
            if (user.getFirstName() == null && attributes.containsKey("given_name")) {
                user.setFirstName((String) attributes.get("given_name"));
            }
            if (user.getLastName() == null && attributes.containsKey("family_name")) {
                user.setLastName((String) attributes.get("family_name"));
            }

            User updatedUser = userRepository.update(user);
            log.info("Linked OAuth to existing account: {}", email);
            return new OAuthResult(updatedUser, false); // Existing user (linked)
        }

        // Create new OAuth user
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        User newUser = User.builder()
                .email(email)
                .firstName(firstName != null ? firstName : "OAuth")
                .lastName(lastName != null ? lastName : "User")
                .emailVerified(true)
                .provider(authProvider)
                .providerId(providerId)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Created new OAuth user: {}", email);
        return new OAuthResult(savedUser, true); // New user
    }

}