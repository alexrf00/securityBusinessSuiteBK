-- path: data/src/main/resources/db/migration/V2__add_oauth_indexes.sql
-- Additional indexes for OAuth optimization
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- Add constraint for provider_id uniqueness per provider
ALTER TABLE users ADD CONSTRAINT unique_provider_id
    UNIQUE NULLS NOT DISTINCT (provider, provider_id);