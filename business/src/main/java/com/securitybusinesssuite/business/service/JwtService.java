// path: business/src/main/java/com/securitybusinesssuite/business/service/JwtService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.TokenPair;

import java.util.UUID;

public interface JwtService {
    TokenPair generateTokenPair(UUID userId, String email);
    String generateAccessToken(UUID userId, String email);
    String generateRefreshToken(UUID userId);
    boolean validateToken(String token);
    UUID getUserIdFromToken(String token);
    String getEmailFromToken(String token);
    String getTokenType(String token);
}