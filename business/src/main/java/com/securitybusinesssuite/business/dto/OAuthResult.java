// path: business/src/main/java/com/securitybusinesssuite/business/dto/OAuthResult.java
package com.securitybusinesssuite.business.dto;

import com.securitybusinesssuite.data.entity.User;

public class OAuthResult {
    private final User user;
    private final boolean isNewUser;

    public OAuthResult(User user, boolean isNewUser) {
        this.user = user;
        this.isNewUser = isNewUser;
    }

    public User getUser() {
        return user;
    }

    public boolean isNewUser() {
        return isNewUser;
    }
}