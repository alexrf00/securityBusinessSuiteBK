-- path: data/src/main/resources/db/migration/V1__create_users_table.sql
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE');

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255),
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       email_verified BOOLEAN DEFAULT FALSE,
                       provider auth_provider NOT NULL DEFAULT 'LOCAL',
                       provider_id VARCHAR(255),
                       email_verification_token VARCHAR(255),
                       email_verification_token_expiry TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(LOWER(email));
CREATE INDEX idx_users_provider_id ON users(provider, provider_id) WHERE provider_id IS NOT NULL;
CREATE INDEX idx_users_verification_token ON users(email_verification_token) WHERE email_verification_token IS NOT NULL;