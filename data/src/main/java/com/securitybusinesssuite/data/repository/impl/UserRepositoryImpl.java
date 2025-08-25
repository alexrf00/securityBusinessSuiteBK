// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/UserRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.User;
import com.securitybusinesssuite.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_USER = """
        INSERT INTO users (id, email, password_hash, first_name, last_name, 
                          email_verified, provider, provider_id, email_verification_token,
                          email_verification_token_expiry, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?::auth_provider, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_USER = """
        UPDATE users SET email = ?, password_hash = ?, first_name = ?, last_name = ?,
                        email_verified = ?, provider = ?::auth_provider, provider_id = ?,
                        email_verification_token = ?, email_verification_token_expiry = ?,
                        updated_at = ?
        WHERE id = ?
        """;

    private static final String SELECT_BY_ID = """
        SELECT * FROM users WHERE id = ?
        """;

    private static final String SELECT_BY_EMAIL = """
        SELECT * FROM users WHERE LOWER(email) = LOWER(?)
        """;

    private static final String SELECT_BY_TOKEN = """
        SELECT * FROM users WHERE email_verification_token = ?
        """;

    private static final String SELECT_BY_PROVIDER = """
        SELECT * FROM users WHERE provider = ?::auth_provider AND provider_id = ?
        """;

    private static final String EXISTS_BY_EMAIL = """
        SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(?))
        """;

    private static final String DELETE_BY_ID = """
        DELETE FROM users WHERE id = ?
        """;

    private final RowMapper<User> userRowMapper = new UserRowMapper();

    @Override
    public User save(User user) {
        user.setId(UUID.randomUUID());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_USER,
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.getProvider().name(),
                user.getProviderId(),
                user.getEmailVerificationToken(),
                user.getEmailVerificationTokenExpiry() != null ?
                        Timestamp.valueOf(user.getEmailVerificationTokenExpiry()) : null,
                Timestamp.valueOf(user.getCreatedAt()),
                Timestamp.valueOf(user.getUpdatedAt())
        );

        return user;
    }

    @Override
    public User update(User user) {
        user.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(UPDATE_USER,
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.getProvider().name(),
                user.getProviderId(),
                user.getEmailVerificationToken(),
                user.getEmailVerificationTokenExpiry() != null ?
                        Timestamp.valueOf(user.getEmailVerificationTokenExpiry()) : null,
                Timestamp.valueOf(user.getUpdatedAt()),
                user.getId()
        );

        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, userRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query(SELECT_BY_EMAIL, userRowMapper, email)
                .stream().findFirst();
    }

    @Override
    public Optional<User> findByEmailVerificationToken(String token) {
        return jdbcTemplate.query(SELECT_BY_TOKEN, userRowMapper, token)
                .stream().findFirst();
    }

    @Override
    public Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId) {
        return jdbcTemplate.query(SELECT_BY_PROVIDER, userRowMapper, provider.name(), providerId)
                .stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_EMAIL, Boolean.class, email);
        return exists != null && exists;
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp tokenExpiry = rs.getTimestamp("email_verification_token_expiry");

            return User.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .email(rs.getString("email"))
                    .passwordHash(rs.getString("password_hash"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .emailVerified(rs.getBoolean("email_verified"))
                    .provider(User.AuthProvider.valueOf(rs.getString("provider")))
                    .providerId(rs.getString("provider_id"))
                    .emailVerificationToken(rs.getString("email_verification_token"))
                    .emailVerificationTokenExpiry(tokenExpiry != null ? tokenExpiry.toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
        }
    }
}