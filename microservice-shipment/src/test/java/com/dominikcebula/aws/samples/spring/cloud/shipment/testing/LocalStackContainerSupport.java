package com.dominikcebula.aws.samples.spring.cloud.shipment.testing;

import lombok.SneakyThrows;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class LocalStackContainerSupport {
    private static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.0.3"))
            .withServices(SNS, SQS);

    @SneakyThrows
    public static void start() {
        LOCAL_STACK_CONTAINER.start();

        execInContainer("awslocal", "sns", "create-topic", "--name", "customer-events-topic");
        execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "customer-events-to-shipment-service");
        execInContainer("awslocal", "sns", "subscribe", "--topic-arn", "arn:aws:sns:us-east-1:000000000000:customer-events-topic", "--protocol", "sqs", "--notification-endpoint", "arn:aws:sqs:us-east-1:000000000000:customer-events-to-shipment-service");
    }

    public static void stop() {
        LOCAL_STACK_CONTAINER.stop();
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.region.static", LOCAL_STACK_CONTAINER::getRegion);
        registry.add("spring.cloud.aws.sns.endpoint", () -> LOCAL_STACK_CONTAINER.getEndpointOverride(SNS));
        registry.add("spring.cloud.aws.sns.region", LOCAL_STACK_CONTAINER::getRegion);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> LOCAL_STACK_CONTAINER.getEndpointOverride(SQS));
        registry.add("spring.cloud.aws.sqs.region", LOCAL_STACK_CONTAINER::getRegion);
    }

    private static void execInContainer(String... command) throws IOException, InterruptedException {
        ContainerSupport.execInContainer(LOCAL_STACK_CONTAINER, command);
    }
}
