-- Run as PostgreSQL administrator
CREATE USER audit_user WITH PASSWORD 'change_me';
CREATE DATABASE audit_log_db OWNER audit_user;
GRANT ALL PRIVILEGES ON DATABASE audit_log_db TO audit_user;
