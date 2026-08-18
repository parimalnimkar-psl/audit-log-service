-- Migration: V2__create_users_table.sql
-- Purpose: Create users table for authentication and user management
-- Security: Store hashed passwords only, use unique index on username

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- ROLE_ADMIN, ROLE_AUDIT_WRITER, ROLE_AUDIT_READER
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    description VARCHAR(255),
    CONSTRAINT chk_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_AUDIT_WRITER', 'ROLE_AUDIT_READER'))
);

-- Indexes for performance
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_active ON users(active);
CREATE INDEX idx_role ON users(role);

-- Insert default admin user (password: admin123)
-- Note: Replace with actual bcrypt hash in production
INSERT INTO users (username, password_hash, role, active, created_by, description) 
VALUES ('admin', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy5QSMM', 'ROLE_ADMIN', true, 'SYSTEM', 'Default admin user');

-- Insert default writer user (password: writer123)
INSERT INTO users (username, password_hash, role, active, created_by, description) 
VALUES ('writer', '$2a$10$sUJQCH1P.C85aLIx3bBLe.H1qNVZR1W5z9q9Y2KJ3pQ8v6x3m7Q0e', 'ROLE_AUDIT_WRITER', true, 'SYSTEM', 'Default writer user');

-- Insert default reader user (password: reader123)
INSERT INTO users (username, password_hash, role, active, created_by, description) 
VALUES ('reader', '$2a$10$dXmF1h7K8j2LpQ5n3rX9BeV8mN6oP4aR9sT2uW5xY3z1A4b6C7d8E', 'ROLE_AUDIT_READER', true, 'SYSTEM', 'Default reader user');
