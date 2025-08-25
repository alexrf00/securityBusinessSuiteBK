// path: business/src/main/java/com/securitybusinesssuite/business/dto/TokenPair.java
package com.securitybusinesssuite.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}