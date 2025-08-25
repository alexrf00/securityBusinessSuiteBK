// path: web/src/main/java/com/securitybusinesssuite/web/controller/AuthController.java
package com.securitybusinesssuite.web.controller;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.service.AuthService;
import com.securitybusinesssuite.business.service.UserService;
import com.securitybusinesssuite.business.util.CookieUtil;
import com.securitybusinesssuite.web.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        TokenPair tokens = authService.login(request);

        // Set cookies
        response.addCookie(cookieUtil.createAccessTokenCookie(tokens.getAccessToken()));
        response.addCookie(cookieUtil.createRefreshTokenCookie(tokens.getRefreshToken()));

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "success", true
        ));
    }

    @GetMapping("/verify-email")
    public void verifyEmail(
            @RequestParam String token,
            HttpServletResponse response) throws IOException {

        try {
            String status = authService.verifyEmail(token);
            response.sendRedirect(frontendUrl + "/auth/verification?status=" + status);
        } catch (Exception e) {
            log.error("Email verification failed", e);
            response.sendRedirect(frontendUrl + "/auth/verification?status=error&message=" + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractTokenFromCookie(request, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token not found"));
        }

        try {
            TokenPair tokens = authService.refresh(refreshToken);

            // Set new cookies
            response.addCookie(cookieUtil.createAccessTokenCookie(tokens.getAccessToken()));
            response.addCookie(cookieUtil.createRefreshTokenCookie(tokens.getRefreshToken()));

            return ResponseEntity.ok(Map.of(
                    "message", "Token refreshed successfully",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        response.addCookie(cookieUtil.createLogoutCookie("access_token"));
        response.addCookie(cookieUtil.createLogoutCookie("refresh_token"));

        return ResponseEntity.ok(Map.of(
                "message", "Logout successful",
                "success", true
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto user = userService.getUserById(UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(user);
    }

    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}