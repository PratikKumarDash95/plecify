-- ============================================================================
-- V2 — Federated authentication (Sign in with Google).
-- Adds provider tracking to users and relaxes the password requirement so
-- provider-verified accounts can exist without a local password hash.
-- ============================================================================

ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_id   VARCHAR(120);

-- Existing rows are local password accounts; the default above covers them.
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- A given external identity maps to exactly one account.
CREATE UNIQUE INDEX uk_users_provider
    ON users (auth_provider, provider_id)
    WHERE provider_id IS NOT NULL;
