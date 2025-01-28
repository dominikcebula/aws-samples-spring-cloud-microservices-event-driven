package com.dominikcebula.aws.samples.spring.cloud.testing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class DefaultLocalStackCredentialsConfiguration {
    @Bean
    public AwsCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(LocalStackContainerSupport.getAwsCredentials());
    }
}
