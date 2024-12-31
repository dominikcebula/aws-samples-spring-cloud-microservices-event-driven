package com.dominikcebula.aws.samples.spring.cloud.customers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.SQLException;

@Configuration
@Profile("aws")
public class RdsIamDataSourceConfiguration {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Bean
    public DataSource dataSource() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(generateAuthToken());

        return dataSource;
    }

    private String generateAuthToken() {
        try (RdsClient rdsClient = RdsClient.builder()
                .region(new DefaultAwsRegionProviderChain().getRegion())
                .build()) {
            URI jdbcUri = parseJdbcURL(dbUrl);

            GenerateAuthenticationTokenRequest request = GenerateAuthenticationTokenRequest.builder()
                    .username(username)
                    .hostname(jdbcUri.getHost())
                    .port(jdbcUri.getPort())
                    .build();

            return rdsClient.utilities()
                    .generateAuthenticationToken(request);
        }
    }

    private URI parseJdbcURL(String jdbcUrl) {
        String uri = jdbcUrl.replace("jdbc:", "");
        return URI.create(uri);
    }
}
