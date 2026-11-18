package com.youtubeagent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DB_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = url;
        }

        HikariConfig config = new HikariConfig();

        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(dbUrl.replace("jdbc:", ""));
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                config.setJdbcUrl(jdbcUrl);
                config.setDriverClassName("org.postgresql.Driver");

                if (uri.getUserInfo() != null) {
                    String[] parts = uri.getUserInfo().split(":");
                    config.setUsername(parts[0]);
                    if (parts.length > 1) {
                        config.setPassword(parts[1]);
                    }
                } else {
                    config.setUsername(username);
                    config.setPassword(password);
                }
            } catch (Exception e) {
                config.setJdbcUrl(dbUrl.startsWith("jdbc:") ? dbUrl : "jdbc:" + dbUrl);
                config.setDriverClassName("org.postgresql.Driver");
                config.setUsername(username);
                config.setPassword(password);
            }
        } else {
            config.setJdbcUrl(dbUrl);
            if (driverClassName != null && !driverClassName.isBlank()) {
                config.setDriverClassName(driverClassName);
            }
            config.setUsername(username);
            config.setPassword(password);
        }

        return new HikariDataSource(config);
    }
}
