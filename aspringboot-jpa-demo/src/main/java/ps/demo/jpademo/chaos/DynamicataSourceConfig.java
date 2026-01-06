package ps.demo.jpademo.chaos;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DynamicataSourceConfig {
    @Bean
    @Primary
    public SwappableDataSource dataSource(DataSourceProperties properties) {
        HikariDataSource initial = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        return new SwappableDataSource(initial);

    }
}