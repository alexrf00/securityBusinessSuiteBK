// path: business/src/main/java/com/securitybusinesssuite/business/util/CookieUtil.java
package com.securitybusinesssuite.business.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    @Value("${app.jwt.access-token-expiration:900}")
    private int accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800}")
    private int refreshTokenExpiration;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.cookie.domain:localhost}")
    private String cookieDomain;

    public Cookie createAccessTokenCookie(String token) {
        return createCookie("access_token", token, accessTokenExpiration);
    }

    public Cookie createRefreshTokenCookie(String token) {
        return createCookie("refresh_token", token, refreshTokenExpiration);
    }

    public Cookie createLogoutCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        return cookie;
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    public String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String getAccessToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, "access_token");
    }

    public String getRefreshToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, "refresh_token");
    }
}