package com.dominikcebula.aws.samples.spring.cloud.shipment.infra;

import com.dominikcebula.aws.samples.spring.cloud.shared.infra.DefaultAwsRdsIamDataSourceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DefaultAwsRdsIamDataSourceConfiguration.class)
public class AwsRdsIamDataSourceConfiguration {
}
