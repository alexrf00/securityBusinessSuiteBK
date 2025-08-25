// path: business/src/main/java/com/securitybusinesssuite/business/dto/AuthResponse.java
package com.securitybusinesssuite.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private UserDto user;
    private boolean requiresEmailVerification;
}