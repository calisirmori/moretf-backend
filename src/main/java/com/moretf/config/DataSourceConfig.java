package com.moretf.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String secretId = "moretf/backend/db";

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.US_EAST_2)
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretId)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);

        Map<String, String> secrets;
        try {
            secrets = new ObjectMapper().readValue(response.secretString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret JSON", e);
        }

        String host = secrets.get("host");
        String port = secrets.get("port");
        String dbname = secrets.get("dbname");
        String username = secrets.get("username");
        String password = secrets.get("password");

        if (host == null || port == null || dbname == null || username == null || password == null) {
            throw new RuntimeException("Missing database secret values.");
        }

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
