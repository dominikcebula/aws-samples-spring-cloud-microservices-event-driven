package com.dominikcebula.aws.samples.spring.cloud.shipment.web;


import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import com.dominikcebula.aws.samples.spring.cloud.testing.PostgreSQLContainerSupport;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.dominikcebula.aws.samples.spring.cloud.shipment.service.ShipmentAddressService.SearchShipmentAddressQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ShipmentAddressControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShipmentAddressRepository shipmentAddressRepository;

    @BeforeAll
    static void beforeAll() {
        PostgreSQLContainerSupport.start();
    }

    @AfterAll
    static void afterAll() {
        PostgreSQLContainerSupport.stop();
    }

    @AfterEach
    void tearDown() {
        shipmentAddressRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyShipmentAddressList() {
        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddressesUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRetrieveShipmentAddresses() {
        // given
        List<ShipmentAddressDTO> shipmentAddressesSavedInDatabase = shipmentAddressesSavedInDatabase();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddressesUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(shipmentAddressesSavedInDatabase.size());
        assertThat(response.getBody())
                .containsOnly(shipmentAddressesSavedInDatabase.toArray(new ShipmentAddressDTO[0]));
    }

    @Test
    void shouldRetrieveOneShipmentAddress() {
        // given
        List<ShipmentAddressDTO> shipmentAddressesSavedInDatabase = shipmentAddressesSavedInDatabase();
        ShipmentAddressDTO shipmentAddressToRetrieve = shipmentAddressesSavedInDatabase.get(2);

        // when
        ResponseEntity<ShipmentAddressDTO> response = restTemplate.exchange(getShipmentAddressesUrl() + "/" + shipmentAddressToRetrieve.getId(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody())
                .isEqualTo(shipmentAddressToRetrieve);
    }

    @Test
    void shouldNotRetrieveNonExistingShipmentAddress() {
        // when
        ResponseEntity<ShipmentAddressDTO> response = restTemplate.exchange(getShipmentAddressesUrl() + "/999", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldSearchShipmentAddressByName() {
        // given
        List<ShipmentAddressDTO> shipmentAddressesSavedInDatabase = shipmentAddressesSavedInDatabase();
        ShipmentAddressDTO shipmentAddressToSearch = shipmentAddressesSavedInDatabase.get(2);
        SearchShipmentAddressQuery searchShipmentAddressQuery = SearchShipmentAddressQuery.builder()
                .firstName(shipmentAddressToSearch.getFirstName())
                .build();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddressesUrl() + "/search", HttpMethod.POST,
                new HttpEntity<>(searchShipmentAddressQuery),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsOnly(shipmentAddressToSearch);
    }

    @Test
    void shouldSearchShipmentAddressByPhone() {
        // given
        List<ShipmentAddressDTO> shipmentAddressesSavedInDatabase = shipmentAddressesSavedInDatabase();
        SearchShipmentAddressQuery searchShipmentAddressQuery = SearchShipmentAddressQuery.builder()
                .phone("67")
                .build();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddressesUrl() + "/search", HttpMethod.POST,
                new HttpEntity<>(searchShipmentAddressQuery),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsOnly(shipmentAddressesSavedInDatabase.get(1), shipmentAddressesSavedInDatabase.get(2));
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
    }

    private List<ShipmentAddressDTO> shipmentAddressesSavedInDatabase() {
        return shipmentAddressRepository.saveAll(createShipmentAddresses());
    }

    private static List<ShipmentAddressDTO> createShipmentAddresses() {
        return List.of(
                createShipmentAddress(100L, "alice@example.com", "Alice", "Smith", "123-456-7890",
                        "123 Main St", "Springfield", "IL", "12345", "USA"),
                createShipmentAddress(200L, "bob@example.com", "Bob", "Johnson", "234-567-8901",
                        "234 Elm St", "Springfield", "IL", "23456", "USA"),
                createShipmentAddress(300L, "carol@example.com", "Carol", "Williams", "345-678-9012",
                        "345 Oak St", "Springfield", "IL", "34567", "USA"),
                createShipmentAddress(400L, "dave@example.com", "Dave", "Brown", "456-789-0123",
                        "456 Pine St", "Springfield", "IL", "45678", "USA")
        );
    }

    private static ShipmentAddressDTO createShipmentAddress(Long customerId, String email, String firstName, String lastName, String phone,
                                                            String street, String city, String state, String zipCode, String country) {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO();
        shipmentAddressDTO.setCustomerId(customerId);
        shipmentAddressDTO.setEmail(email);
        shipmentAddressDTO.setFirstName(firstName);
        shipmentAddressDTO.setLastName(lastName);
        shipmentAddressDTO.setPhone(phone);
        shipmentAddressDTO.setStreet(street);
        shipmentAddressDTO.setCity(city);
        shipmentAddressDTO.setState(state);
        shipmentAddressDTO.setZipCode(zipCode);
        shipmentAddressDTO.setCountry(country);
        return shipmentAddressDTO;
    }
}
