package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.AddressEventData;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import com.dominikcebula.aws.samples.spring.cloud.shipment.testing.LocalStackContainerSupport;
import com.dominikcebula.aws.samples.spring.cloud.shipment.testing.PostgreSQLContainerSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.ExecutionException;

import static com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEventType.CREATED;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles({"local", "test"})
class CustomerEventConsumerIntegrationTest {
    @Autowired
    private StreamBridge streamBridge;
    @SpyBean
    private CustomerEventConsumer customerEventConsumer;

    @BeforeAll
    static void beforeAll() {
        PostgreSQLContainerSupport.start();
        LocalStackContainerSupport.start();
    }

    @AfterAll
    static void afterAll() {
        LocalStackContainerSupport.stop();
        PostgreSQLContainerSupport.stop();
    }

    @Test
    void shouldProcessCustomerCreatedEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        // given
        CustomerEventData customerEventData = createCustomerEventData();
        CustomerEvent customerCreatedEvent = new CustomerEvent(CREATED, customerEventData);

        // when
        publishEvent(customerCreatedEvent);
        waitUntilEventConsumed();

        // then


    }

    private CustomerEventData createCustomerEventData() {
        return new CustomerEventData(
                100L,
                "John",
                "Doe",
                "John.Doe@mail.com",
                "+123456789",
                new AddressEventData(100L, "123 Main St", "Springfield", "IL", "12345", "USA"),
                new AddressEventData(101L, "234 Elm St", "Springfield", "IL", "23456", "USA")
        );
    }

    private void publishEvent(CustomerEvent event) {
        streamBridge.send("customerEvents-out-0", event);
    }

    private void waitUntilEventConsumed() {
        verify(customerEventConsumer, timeout(5000)).accept(any(CustomerEvent.class));
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainerSupport.registerProperties(registry);
        LocalStackContainerSupport.registerProperties(registry);
    }
}
