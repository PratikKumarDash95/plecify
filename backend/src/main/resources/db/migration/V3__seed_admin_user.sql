-- ============================================================================
-- V3 — Seed the bootstrap platform administrator.
--
-- Registration only creates STUDENT/COMPANY accounts and ADMIN self-signup is
-- deliberately not exposed, so the first admin must be seeded. Credentials come
-- from Flyway placeholders wired to the ADMIN_EMAIL / ADMIN_PASSWORD env vars
-- (see spring.flyway.placeholders in application.yml); the defaults are for
-- local development only and MUST be overridden in any shared environment.
--
-- The password is hashed with pgcrypto's bcrypt (gen_salt('bf', 12)), which
-- emits a $2a$ hash compatible with Spring's BCryptPasswordEncoder(12).
-- Idempotent: re-running (or a fresh baseline) never creates a duplicate admin.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, email, password_hash, full_name, enabled, email_verified,
                   account_locked, failed_login_attempts, auth_provider,
                   created_at, updated_at, version)
SELECT gen_random_uuid(),
       LOWER('${adminEmail}'),
       crypt('${adminPassword}', gen_salt('bf', 12)),
       'Platform Administrator',
       TRUE,   -- enabled
       TRUE,   -- email_verified: seeded admin skips the email flow
       FALSE,
       0,
       'LOCAL',
       now(),
       now(),
       0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = LOWER('${adminEmail}'));

-- Grant the ADMIN role (seeded in V1) to that user.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = LOWER('${adminEmail}')
  AND r.name = 'ADMIN'
  AND NOT EXISTS (
        SELECT 1 FROM user_roles ur
        WHERE ur.user_id = u.id AND ur.role_id = r.id);
