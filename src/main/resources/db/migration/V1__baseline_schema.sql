-- ============================================================================
-- V1 — Baseline schema for the Campus Recruitment Portal.
-- Postgres dialect (Supabase-compatible). Mirrors the JPA entity model so that
-- Hibernate's ddl-auto=validate passes at startup.
-- ============================================================================

-- ---------------------------------------------------------------- reference: roles
CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(40)  NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

-- ---------------------------------------------------------------- universities
CREATE TABLE universities (
    id           UUID PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    code         VARCHAR(30)  NOT NULL,
    email_domain VARCHAR(120),
    city         VARCHAR(120),
    country      VARCHAR(120),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120),
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_universities_code   UNIQUE (code),
    CONSTRAINT uk_universities_domain UNIQUE (email_domain)
);
CREATE INDEX idx_universities_code ON universities (code);

-- ---------------------------------------------------------------- users
CREATE TABLE users (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(180) NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    full_name             VARCHAR(150) NOT NULL,
    phone                 VARCHAR(20),
    enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    account_locked        BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER      NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(120),
    updated_by            VARCHAR(120),
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE INDEX idx_users_email   ON users (email);
CREATE INDEX idx_users_enabled ON users (enabled);

-- ---------------------------------------------------------------- user_roles (join)
CREATE TABLE user_roles (
    user_id UUID    NOT NULL,
    role_id INTEGER NOT NULL,
    CONSTRAINT uk_user_roles UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- ---------------------------------------------------------------- companies
CREATE TABLE companies (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL,
    name          VARCHAR(200) NOT NULL,
    industry      VARCHAR(120),
    website       VARCHAR(255),
    description   TEXT,
    logo_url      VARCHAR(512),
    headquarters  VARCHAR(200),
    contact_email VARCHAR(180),
    contact_phone VARCHAR(20),
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_companies_user UNIQUE (user_id),
    CONSTRAINT fk_companies_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_companies_status ON companies (status);
CREATE INDEX idx_companies_name   ON companies (name);

-- ---------------------------------------------------------------- placement_cells
CREATE TABLE placement_cells (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL,
    university_id UUID         NOT NULL,
    office_name   VARCHAR(150) NOT NULL,
    contact_email VARCHAR(180),
    contact_phone VARCHAR(20),
    status        VARCHAR(20)  NOT NULL DEFAULT 'APPROVED',
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_placement_cells_user UNIQUE (user_id),
    CONSTRAINT fk_placement_cells_user       FOREIGN KEY (user_id)       REFERENCES users (id),
    CONSTRAINT fk_placement_cells_university FOREIGN KEY (university_id) REFERENCES universities (id)
);
CREATE INDEX idx_placement_cells_university ON placement_cells (university_id);

-- ---------------------------------------------------------------- students
CREATE TABLE students (
    id                 UUID PRIMARY KEY,
    user_id            UUID          NOT NULL,
    university_id      UUID          NOT NULL,
    roll_number        VARCHAR(40)   NOT NULL,
    department         VARCHAR(100)  NOT NULL,
    branch             VARCHAR(100)  NOT NULL,
    degree             VARCHAR(60),
    cgpa               NUMERIC(4,2)  NOT NULL,
    active_backlogs    INTEGER       NOT NULL DEFAULT 0,
    total_backlogs     INTEGER       NOT NULL DEFAULT 0,
    passing_year       INTEGER       NOT NULL,
    work_authorization VARCHAR(30)   NOT NULL DEFAULT 'CITIZEN',
    location           VARCHAR(120),
    gender             VARCHAR(20)   DEFAULT 'UNDISCLOSED',
    date_of_birth      DATE,
    resume_url         VARCHAR(512),
    avatar_url         VARCHAR(512),
    placement_eligible BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL,
    created_by         VARCHAR(120),
    updated_by         VARCHAR(120),
    version            BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_students_user UNIQUE (user_id),
    CONSTRAINT uk_students_roll UNIQUE (university_id, roll_number),
    CONSTRAINT fk_students_user       FOREIGN KEY (user_id)       REFERENCES users (id),
    CONSTRAINT fk_students_university FOREIGN KEY (university_id) REFERENCES universities (id)
);
CREATE INDEX idx_students_university         ON students (university_id);
CREATE INDEX idx_students_dept_branch        ON students (department, branch);
CREATE INDEX idx_students_passing_year       ON students (passing_year);
CREATE INDEX idx_students_cgpa               ON students (cgpa);
CREATE INDEX idx_students_placement_eligible ON students (placement_eligible);

CREATE TABLE student_skills (
    student_id UUID        NOT NULL,
    skill      VARCHAR(80) NOT NULL,
    CONSTRAINT uk_student_skills UNIQUE (student_id, skill),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- jobs
CREATE TABLE jobs (
    id                      UUID PRIMARY KEY,
    company_id              UUID         NOT NULL,
    placement_cell_id       UUID         NOT NULL,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT         NOT NULL,
    job_type                VARCHAR(30)  NOT NULL,
    location                VARCHAR(150),
    remote_allowed          BOOLEAN      NOT NULL DEFAULT FALSE,
    salary_min              NUMERIC(12,2),
    salary_max              NUMERIC(12,2),
    currency                VARCHAR(3)   DEFAULT 'USD',
    openings                INTEGER      NOT NULL DEFAULT 1,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    application_deadline    TIMESTAMPTZ  NOT NULL,
    rejection_reason        VARCHAR(500),
    reviewed_at             TIMESTAMPTZ,
    approved_by             UUID,
    approved_at             TIMESTAMPTZ,
    eligibility_computed_at TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    created_by              VARCHAR(120),
    updated_by              VARCHAR(120),
    version                 BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_jobs_company        FOREIGN KEY (company_id)        REFERENCES companies (id),
    CONSTRAINT fk_jobs_placement_cell FOREIGN KEY (placement_cell_id) REFERENCES placement_cells (id)
);
CREATE INDEX idx_jobs_status          ON jobs (status);
CREATE INDEX idx_jobs_company         ON jobs (company_id);
CREATE INDEX idx_jobs_placement_cell  ON jobs (placement_cell_id);
CREATE INDEX idx_jobs_deadline        ON jobs (application_deadline);
CREATE INDEX idx_jobs_status_deadline ON jobs (status, application_deadline);

-- ---------------------------------------------------------------- job_eligibility
CREATE TABLE job_eligibility (
    id                          UUID PRIMARY KEY,
    job_id                      UUID         NOT NULL,
    min_cgpa                    NUMERIC(4,2),
    max_active_backlogs         INTEGER,
    max_total_backlogs          INTEGER,
    required_work_authorization VARCHAR(30)  DEFAULT 'ANY',
    skill_match_mode            VARCHAR(10)  DEFAULT 'ANY',
    min_age                     INTEGER,
    max_age                     INTEGER,
    min_package                 NUMERIC(12,2),
    max_package                 NUMERIC(12,2),
    CONSTRAINT uk_job_eligibility_job UNIQUE (job_id),
    CONSTRAINT fk_job_eligibility_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
);

CREATE TABLE job_eligible_departments (
    job_eligibility_id UUID         NOT NULL,
    department         VARCHAR(100),
    CONSTRAINT fk_job_elig_departments FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_eligible_branches (
    job_eligibility_id UUID         NOT NULL,
    branch             VARCHAR(100),
    CONSTRAINT fk_job_elig_branches FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_eligible_passing_years (
    job_eligibility_id UUID    NOT NULL,
    passing_year       INTEGER,
    CONSTRAINT fk_job_elig_years FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_required_skills (
    job_eligibility_id UUID        NOT NULL,
    skill              VARCHAR(80),
    CONSTRAINT fk_job_elig_skills FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_eligible_locations (
    job_eligibility_id UUID         NOT NULL,
    location           VARCHAR(120),
    CONSTRAINT fk_job_elig_locations FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_eligible_genders (
    job_eligibility_id UUID        NOT NULL,
    gender             VARCHAR(20),
    CONSTRAINT fk_job_elig_genders FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);
CREATE TABLE job_eligible_batches (
    job_eligibility_id UUID        NOT NULL,
    batch              VARCHAR(40),
    CONSTRAINT fk_job_elig_batches FOREIGN KEY (job_eligibility_id) REFERENCES job_eligibility (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------- eligible_jobs
CREATE TABLE eligible_jobs (
    id         UUID PRIMARY KEY,
    student_id UUID        NOT NULL,
    job_id     UUID        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'ELIGIBLE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    CONSTRAINT uk_eligible_jobs_student_job UNIQUE (student_id, job_id),
    CONSTRAINT fk_eligible_jobs_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_eligible_jobs_job     FOREIGN KEY (job_id)     REFERENCES jobs (id)     ON DELETE CASCADE
);
CREATE INDEX idx_eligible_jobs_student_status ON eligible_jobs (student_id, status);
CREATE INDEX idx_eligible_jobs_job            ON eligible_jobs (job_id);

-- ---------------------------------------------------------------- applications
CREATE TABLE applications (
    id                    UUID PRIMARY KEY,
    student_id            UUID         NOT NULL,
    job_id                UUID         NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'APPLIED',
    resume_url            VARCHAR(512),
    cover_letter          TEXT,
    interview_at          TIMESTAMPTZ,
    interview_details     VARCHAR(1000),
    status_note           VARCHAR(1000),
    last_status_change_at TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(120),
    updated_by            VARCHAR(120),
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_applications_student_job UNIQUE (student_id, job_id),
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_applications_job     FOREIGN KEY (job_id)     REFERENCES jobs (id)
);
CREATE INDEX idx_applications_job_status ON applications (job_id, status);
CREATE INDEX idx_applications_student    ON applications (student_id);
CREATE INDEX idx_applications_status     ON applications (status);

-- ---------------------------------------------------------------- application_status_history
CREATE TABLE application_status_history (
    id             UUID PRIMARY KEY,
    application_id UUID         NOT NULL,
    from_status    VARCHAR(30),
    to_status      VARCHAR(30)  NOT NULL,
    note           VARCHAR(1000),
    changed_by     VARCHAR(120),
    created_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_app_status_history_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
);
CREATE INDEX idx_app_status_history_application ON application_status_history (application_id, created_at);

-- ---------------------------------------------------------------- notifications
CREATE TABLE notifications (
    id           UUID PRIMARY KEY,
    recipient_id UUID          NOT NULL,
    type         VARCHAR(40)   NOT NULL,
    title        VARCHAR(200)  NOT NULL,
    body         VARCHAR(1000) NOT NULL,
    link         VARCHAR(512),
    read_flag    BOOLEAN       NOT NULL DEFAULT FALSE,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ   NOT NULL,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_notifications_recipient_read    ON notifications (recipient_id, read_flag);
CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_id, created_at);

-- ---------------------------------------------------------------- refresh_tokens
CREATE TABLE refresh_tokens (
    id                     UUID PRIMARY KEY,
    user_id                UUID         NOT NULL,
    token_hash             VARCHAR(100) NOT NULL,
    expires_at             TIMESTAMPTZ  NOT NULL,
    revoked                BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at             TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(100),
    user_agent             VARCHAR(300),
    ip_address             VARCHAR(60),
    created_at             TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_refresh_tokens_user    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

-- ---------------------------------------------------------------- verification_tokens
CREATE TABLE verification_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    token_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_verification_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_verification_tokens_user_type ON verification_tokens (user_id, type);
CREATE INDEX idx_verification_tokens_expires   ON verification_tokens (expires_at);

-- ---------------------------------------------------------------- email_logs
CREATE TABLE email_logs (
    id                  UUID PRIMARY KEY,
    recipient_email     VARCHAR(180) NOT NULL,
    subject             VARCHAR(300) NOT NULL,
    template            VARCHAR(80),
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    provider_message_id VARCHAR(200),
    error_message       VARCHAR(1000),
    attempts            INTEGER      NOT NULL DEFAULT 0,
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_email_logs_recipient ON email_logs (recipient_email, created_at);
CREATE INDEX idx_email_logs_status    ON email_logs (status);

-- ---------------------------------------------------------------- audit_logs
CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY,
    actor_email VARCHAR(180),
    actor_id    UUID,
    action      VARCHAR(80) NOT NULL,
    target_type VARCHAR(60),
    target_id   VARCHAR(80),
    details     TEXT,
    ip_address  VARCHAR(60),
    success     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_audit_logs_actor  ON audit_logs (actor_email, created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs (action, created_at);
CREATE INDEX idx_audit_logs_target ON audit_logs (target_type, target_id);

-- ---------------------------------------------------------------- seed roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN',          'Platform administrator'),
    ('PLACEMENT_CELL', 'University placement cell officer'),
    ('COMPANY',        'Recruiting company'),
    ('STUDENT',        'Student candidate');
