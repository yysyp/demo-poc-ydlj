package com.poc.mpt.chaos;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Component
public class DbHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    public DbHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select 1")) {
            ps.execute();
            return Health.up().build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}