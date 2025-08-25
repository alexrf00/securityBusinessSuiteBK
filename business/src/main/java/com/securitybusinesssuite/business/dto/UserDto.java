// path: business/src/main/java/com/securitybusinesssuite/business/dto/UserDto.java
package com.securitybusinesssuite.business.dto;

import com.securitybusinesssuite.data.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
    private User.AuthProvider provider;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}