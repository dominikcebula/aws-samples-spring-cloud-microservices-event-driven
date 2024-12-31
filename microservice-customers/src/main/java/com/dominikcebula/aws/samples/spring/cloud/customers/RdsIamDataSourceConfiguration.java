package com.dominikcebula.aws.samples.spring.cloud.customers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;
import software.amazon.awssdk.services.sts.StsClient;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.SQLException;

@Configuration
@Profile("aws")
@Log
public class RdsIamDataSourceConfiguration {
    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Bean
    public DataSource dataSource() throws SQLException {
        log.info("Creating RDS IAM DataSource");

        log.info("STS Caller Identity: " + getCallerIdentity());

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(generateAuthToken());

        log.info("RDS IAM URL: " + dataSource.getUrl());
        log.info("RDS IAM Username: " + dataSource.getUsername());
        log.info("RDS IAM Password: " + dataSource.getPassword());

        log.info("RDS IAM DataSource created");

        return dataSource;
    }

    private String getCallerIdentity() {
        try (StsClient stsClient = StsClient.builder().credentialsProvider(DefaultCredentialsProvider.create()).build()) {
            return stsClient.getCallerIdentity().toString();
        }
    }

    private String generateAuthToken() {
        Region region = new DefaultAwsRegionProviderChain().getRegion();

        log.info("Generating RDS IAM token for region: " + region);

        try (RdsClient rdsClient = RdsClient.builder()
                .region(region)
                .build()) {
            URI dbUri = parseJdbcURL(jdbcUrl);

            GenerateAuthenticationTokenRequest request = GenerateAuthenticationTokenRequest.builder()
                    .username(username)
                    .hostname(dbUri.getHost())
                    .port(dbUri.getPort())
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
