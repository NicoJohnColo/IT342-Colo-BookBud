package edu.cit.colo.bookbud.shared.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationService {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            log.info("Running database migration to fix payment_method constraint...");
            // Drop the old check constraint that prevents "Stripe_Card" from being saved
            jdbcTemplate.execute("ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_method_check");
            log.info("Successfully dropped payments_payment_method_check constraint.");
        } catch (Exception e) {
            log.warn("Could not drop constraint (it might not exist): {}", e.getMessage());
        }
    }
}
