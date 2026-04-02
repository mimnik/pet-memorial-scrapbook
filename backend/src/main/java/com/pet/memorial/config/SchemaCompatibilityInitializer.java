package com.pet.memorial.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        executeIgnoreError("ALTER TABLE users ADD COLUMN IF NOT EXISTS account_frozen BOOLEAN NOT NULL DEFAULT FALSE");
        executeIgnoreError("ALTER TABLE users ADD COLUMN IF NOT EXISTS posting_restricted BOOLEAN NOT NULL DEFAULT FALSE");
        executeIgnoreError("ALTER TABLE users ADD COLUMN IF NOT EXISTS warning_count INTEGER NOT NULL DEFAULT 0");
        executeIgnoreError("ALTER TABLE users ADD COLUMN IF NOT EXISTS admin_note VARCHAR(1000)");

        executeIgnoreError("ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden_by_admin BOOLEAN NOT NULL DEFAULT FALSE");
        executeIgnoreError("ALTER TABLE community_comments ADD COLUMN IF NOT EXISTS hidden_by_admin BOOLEAN NOT NULL DEFAULT FALSE");

        executeIgnoreError("""
            CREATE TABLE IF NOT EXISTS user_account_appeals (
              id BIGINT PRIMARY KEY AUTO_INCREMENT,
              username VARCHAR(100) NOT NULL,
              appeal_type VARCHAR(40) NOT NULL,
              details VARCHAR(2000) NOT NULL,
              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
              handled_by_username VARCHAR(100),
              handled_at DATETIME,
              handle_note VARCHAR(1000),
              created_at DATETIME NOT NULL,
              updated_at DATETIME NOT NULL
            )
            """);

        executeIgnoreError("UPDATE users SET account_frozen = FALSE WHERE account_frozen IS NULL");
        executeIgnoreError("UPDATE users SET posting_restricted = FALSE WHERE posting_restricted IS NULL");
        executeIgnoreError("UPDATE users SET warning_count = 0 WHERE warning_count IS NULL");
    }

    private void executeIgnoreError(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Best-effort compatibility migration for legacy local databases.
        }
    }
}
