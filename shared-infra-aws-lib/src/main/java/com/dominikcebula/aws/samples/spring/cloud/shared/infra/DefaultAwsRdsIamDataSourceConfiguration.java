package com.dominikcebula.aws.samples.spring.cloud.shared.infra;

import lombok.extern.slf4j.Slf4j;
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

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("aws")
@Slf4j
public class DefaultAwsRdsIamDataSourceConfiguration {
    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Bean
    public DataSource dataSource() {
        log.info("Creating RDS IAM DataSource");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(generateAuthToken());

        log.info("RDS IAM DataSource created");

        return dataSource;
    }

    private String generateAuthToken() {
        Region region = new DefaultAwsRegionProviderChain().getRegion();

        try (RdsClient rdsClient = RdsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
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
