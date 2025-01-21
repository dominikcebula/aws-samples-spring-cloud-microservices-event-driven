package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerDeletedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerUpdatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.AddressEventData;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import com.dominikcebula.aws.samples.spring.cloud.testing.LocalStackContainerSupport;
import com.dominikcebula.aws.samples.spring.cloud.testing.PostgreSQLContainerSupport;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles({"local", "test"})
class CustomerEventConsumerIntegrationTest {
    @Autowired
    private StreamBridge streamBridge;

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ShipmentAddressRepository shipmentAddressRepository;

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

    @AfterEach
    void afterEach() {
        shipmentAddressRepository.deleteAll();
    }

    @Test
    void shouldProcessCustomerCreatedEvent() {
        // given
        CustomerEventData customerEventData = createCustomerEventData();
        CustomerCreatedEvent customerCreatedEvent = new CustomerCreatedEvent(customerEventData);

        // when
        publishEvent(customerCreatedEvent);

        // then
        assertShipmentAddressData(customerCreatedEvent.getCustomerEventData());
    }

    @Test
    void shouldProcessCustomerUpdatedEvent() {
        // given
        CustomerEventData existingCustomerEventData = createCustomerEventData();
        CustomerEventData updatedCustomerEventData = createCustomerEventData();
        updatedCustomerEventData.setFirstName("Bob");
        updatedCustomerEventData.getDeliveryAddress().setCountry("Poland");
        createShipmentAddressInDb();
        CustomerUpdatedEvent customerUpdatedEvent = new CustomerUpdatedEvent(existingCustomerEventData, updatedCustomerEventData);

        // when
        publishEvent(customerUpdatedEvent);

        // then
        assertShipmentAddressData(updatedCustomerEventData);
    }

    @Test
    void shouldProcessCustomerDeletedEvent() {
        // given
        CustomerEventData customerEventData = createCustomerEventData();
        ShipmentAddressDTO shipmentAddressInDb = createShipmentAddressInDb();
        CustomerDeletedEvent customerDeletedEvent = new CustomerDeletedEvent(customerEventData);

        // when
        publishEvent(customerDeletedEvent);

        // then
        assertShipmentAddressDeleted(shipmentAddressInDb);
    }

    private CustomerEventData createCustomerEventData() {
        return new CustomerEventData(
                10L,
                "John",
                "Doe",
                "John.Doe@mail.com",
                "+123456789",
                new AddressEventData(101L, "123 Main St", "Springfield", "IL", "12345", "USA"),
                new AddressEventData(102L, "234 Elm St", "Springfield", "IL", "23456", "USA")
        );
    }

    private ShipmentAddressDTO createShipmentAddressInDb() {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO(
                102L, 10L,
                "John", "Doe", "John.Doe@mail.com", "+123456789",
                "234 Elm St", "Springfield", "IL", "23456", "USA"
        );

        return shipmentAddressRepository.save(shipmentAddressDTO);
    }

    private void publishEvent(CustomerEvent event) {
        streamBridge.send("customerEvents-out-0", event);
    }

    private void assertShipmentAddressData(CustomerEventData customerEventData) {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<ShipmentAddressDTO> response = getShipmentAddressById(customerEventData.getDeliveryAddress().getAddressId());
            ShipmentAddressDTO retrievedShipmentAddress = response.getBody();

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(retrievedShipmentAddress)
                    .isNotNull();
            assertShipmentAddressMatchesEventData(retrievedShipmentAddress, customerEventData);
        });
    }

    private void assertShipmentAddressDeleted(ShipmentAddressDTO shipmentAddressDTO) {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<ShipmentAddressDTO> response = getShipmentAddressById(shipmentAddressDTO.getAddressId());

            assertThat(shipmentAddressRepository.findById(shipmentAddressDTO.getAddressId()))
                    .isEmpty();
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    private static void assertShipmentAddressMatchesEventData(ShipmentAddressDTO retrievedShipmentAddress, CustomerEventData customerEventData) {
        assertThat(retrievedShipmentAddress.getAddressId())
                .isEqualTo(customerEventData.getDeliveryAddress().getAddressId());
        assertThat(retrievedShipmentAddress.getCustomerId())
                .isEqualTo(customerEventData.getCustomerId());
        assertThat(retrievedShipmentAddress)
                .usingRecursiveComparison()
                .comparingOnlyFields("firstName", "lastName", "email", "phone")
                .isEqualTo(customerEventData);
        assertThat(retrievedShipmentAddress)
                .usingRecursiveComparison()
                .comparingOnlyFields("street", "city", "state", "zipCode", "country")
                .isEqualTo(customerEventData.getDeliveryAddress());
    }

    private ResponseEntity<ShipmentAddressDTO> getShipmentAddressById(Long id) {
        return restTemplate.exchange(getShipmentAddressesUrl() + "/" + id, HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
    }

    private @NotNull String getShipmentAddressesUrl() {
        return getBaseUrl() + "/shipment/addresses";
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainerSupport.registerProperties(registry);
        LocalStackContainerSupport.registerProperties(registry);
    }
}
