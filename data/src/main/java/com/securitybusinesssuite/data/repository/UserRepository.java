// path: data/src/main/java/com/securitybusinesssuite/data/repository/UserRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    User update(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}