// path: business/src/main/java/com/securitybusinesssuite/business/service/AuthService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.data.entity.User;

import java.util.Map;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    TokenPair login(LoginRequest request);
    String verifyEmail(String token);
    TokenPair refresh(String refreshToken);
    User processOAuthLogin(String email, String provider, String providerId, Map<String, Object> attributes);
}