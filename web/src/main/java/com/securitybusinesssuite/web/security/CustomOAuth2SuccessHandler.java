// path: web/src/main/java/com/securitybusinesssuite/web/security/CustomOAuth2SuccessHandler.java
package com.securitybusinesssuite.web.security;

import com.securitybusinesssuite.business.dto.OAuthResult;
import com.securitybusinesssuite.business.service.AuthService;
import com.securitybusinesssuite.business.service.JwtService;
import com.securitybusinesssuite.business.util.CookieUtil;
import com.securitybusinesssuite.data.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Add @Lazy to the constructor to break the circular dependency
    public CustomOAuth2SuccessHandler(@Lazy AuthService authService,
                                      JwtService jwtService,
                                      CookieUtil cookieUtil) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String provider = "GOOGLE";
        String providerId = oAuth2User.getName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        try {
            OAuthResult result = authService.processOAuthLogin(email, provider, providerId, attributes);
            User user = result.getUser();

            // Generate JWT tokens
            var tokens = jwtService.generateTokenPair(user.getId(), user.getEmail());

            // Set cookies
            response.addCookie(cookieUtil.createAccessTokenCookie(tokens.getAccessToken()));
            response.addCookie(cookieUtil.createRefreshTokenCookie(tokens.getRefreshToken()));

            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            // Redirect based on user status
            if (result.isNewUser()) {
                // First time registration - redirect to callback page
                getRedirectStrategy().sendRedirect(request, response,
                        frontendUrl + "/auth/callback?status=success&email=" + encodedEmail);
            } else {
                // Existing user login - redirect to dashboard
                getRedirectStrategy().sendRedirect(request, response,
                        frontendUrl + "/dashboard");
            }

        } catch (Exception e) {
            log.error("OAuth authentication failed", e);
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/auth/callback?status=error&message=" + encodedMessage);
        }
    }
}