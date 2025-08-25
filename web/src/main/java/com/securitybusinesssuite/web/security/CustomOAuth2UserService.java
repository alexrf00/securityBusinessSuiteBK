// path: web/src/main/java/com/securitybusinesssuite/web/security/CustomOAuth2UserService.java
package com.securitybusinesssuite.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        String email = user.getAttribute("email");
        // Log OAuth user info for debugging
        log.info("OAuth2 user loaded: {}", email!=null?email:"unknown");

        // Additional processing can be done here if needed
        return user;
    }
}