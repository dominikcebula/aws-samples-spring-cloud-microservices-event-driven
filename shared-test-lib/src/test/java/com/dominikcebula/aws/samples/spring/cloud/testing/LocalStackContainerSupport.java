package com.dominikcebula.aws.samples.spring.cloud.testing;

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

    public static final String TOPIC_CUSTOMER_EVENTS = "customer-events-topic";
    public static final String QUEUE_CUSTOMER_EVENTS_TO_TEST_CONSUMER = "customer-events-to-test-consumer";
    public static final String QUEUE_CUSTOMER_EVENTS_TO_SHIPMENT_SERVICE = "customer-events-to-shipment-service";

    @SneakyThrows
    public static void start() {
        LOCAL_STACK_CONTAINER.start();

        createTopic(TOPIC_CUSTOMER_EVENTS);
        createAndSubscribeQueue(TOPIC_CUSTOMER_EVENTS, QUEUE_CUSTOMER_EVENTS_TO_SHIPMENT_SERVICE);
        createAndSubscribeQueue(TOPIC_CUSTOMER_EVENTS, QUEUE_CUSTOMER_EVENTS_TO_TEST_CONSUMER);
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

    private static void createTopic(String topicName) throws IOException, InterruptedException {
        execInContainer("awslocal", "sns", "create-topic", "--name", topicName);
    }

    private static void createAndSubscribeQueue(String topicName, String queueName) throws IOException, InterruptedException {
        execInContainer("awslocal", "sqs", "create-queue", "--queue-name", queueName);
        execInContainer("awslocal", "sns", "subscribe", "--topic-arn", topicArn(topicName), "--protocol", "sqs", "--notification-endpoint", queueArn(queueName));
    }

    private static String topicArn(String topicName) {
        return "arn:aws:sns:us-east-1:000000000000:" + topicName;
    }

    private static String queueArn(String queueArn) {
        return "arn:aws:sqs:us-east-1:000000000000:" + queueArn;
    }

    private static void execInContainer(String... command) throws IOException, InterruptedException {
        ContainerSupport.execInContainer(LOCAL_STACK_CONTAINER, command);
    }
}
