package laboratorio.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // ========== POSTGRESQL (Primary - Para JPA) ==========
    @Bean(name = "primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    // ========== SQL SERVER (External - Para Consultas) ==========
    @Bean(name = "externalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.external")
    public DataSource externalDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    // ========== JdbcTemplate para SQL Server ==========
    @Bean(name = "externalJdbcTemplate")
    public JdbcTemplate externalJdbcTemplate(@Qualifier("externalDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
