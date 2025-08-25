// path: web/src/main/java/com/securitybusinesssuite/web/security/CustomOAuth2SuccessHandler.java
package com.securitybusinesssuite.web.security;

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
            User user = authService.processOAuthLogin(email, provider, providerId, attributes);

            // Generate JWT tokens
            var tokens = jwtService.generateTokenPair(user.getId(), user.getEmail());

            // Set cookies
            response.addCookie(cookieUtil.createAccessTokenCookie(tokens.getAccessToken()));
            response.addCookie(cookieUtil.createRefreshTokenCookie(tokens.getRefreshToken()));

            // Redirect to frontend
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/auth/callback?status=success");

        } catch (Exception e) {
            log.error("OAuth authentication failed", e);
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/auth/callback?status=error&message=" + e.getMessage());
        }
    }
}