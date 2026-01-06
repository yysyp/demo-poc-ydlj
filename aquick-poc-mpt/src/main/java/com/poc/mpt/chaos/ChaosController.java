package com.poc.mpt.chaos;


import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/actuator/api/v1/chaos")
@Profile({"default", "local", "dev"})
public class ChaosController {
    private final SwappableDataSource swappableDataSource;
    private final String originaldbcUrl;
    private final String brokenJdbcurt = "Jdbc:h2://badip:443/mydb";

    public ChaosController(SwappableDataSource swappableDataSource,
                           @Value("${spring.datasource.url}") String originaldbcUrl) {
        this.swappableDataSource = swappableDataSource;
        this.originaldbcUrl = originaldbcUrl;
    }

    @PostMapping("/db/down")
    public ResponseEntity<String> dbDown() {
        HikariDataSource current = (HikariDataSource) swappableDataSource.getTarget();
        HikariDataSource next = cloneWithUrl(current, brokenJdbcurt);
        swappableDataSource.swap(next);
        return ResponseEntity.ok("DB URL switched to a broken one.");
    }

    @PostMapping("/db/up")
    public ResponseEntity<String> dUp() {
        HikariDataSource current = (HikariDataSource) swappableDataSource.getTarget();
        HikariDataSource next = cloneWithUrl(current, originaldbcUrl);
        swappableDataSource.swap(next);
        return ResponseEntity.ok("DB URL restored.");
    }

    private HikariDataSource cloneWithUrl(HikariDataSource base, String url) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(base.getUsername());
        ds.setPassword(base.getPassword());
        ds.setDriverClassName(base.getDriverClassName());
        ds.setMaximumPoolSize(base.getMaximumPoolSize());
        ds.setMinimumIdle(base.getMinimumIdle());
        ds.setConnectionTimeout(base.getConnectionTimeout());
        ds.setValidationTimeout(base.getValidationTimeout());
        ds.setIdleTimeout(base.getIdleTimeout());
        ds.setMaxLifetime(base.getMaxLifetime());
        ds.setConnectionTestQuery(base.getConnectionTestQuery());
        return ds;
    }
}