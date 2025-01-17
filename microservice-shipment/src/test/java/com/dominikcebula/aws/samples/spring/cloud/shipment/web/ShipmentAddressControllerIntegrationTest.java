package com.dominikcebula.aws.samples.spring.cloud.shipment.web;


import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.dominikcebula.aws.samples.spring.cloud.shipment.service.ShipmentAddressService.SearchShipmentAddressQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ShipmentAddressControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShipmentAddressRepository shipmentAddressRepository;

    @Autowired
    private SqsAsyncClient amazonSQS;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17");
    @Container
    private static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.0.3"))
            .withServices(SNS, SQS);

    @BeforeAll
    static void beforeAll() throws Exception {
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sns", "create-topic", "--name", "shipmentAddress-events-topic");
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "shipmentAddress-events");
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sns", "subscribe", "--topic-arn", "arn:aws:sns:us-east-1:000000000000:shipmentAddress-events-topic", "--protocol", "sqs", "--notification-endpoint", "arn:aws:sqs:us-east-1:000000000000:shipmentAddress-events");
    }

    @AfterEach
    void tearDown() {
        shipmentAddressRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyShipmentAddresssList() {
        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddresssUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRetrieveShipmentAddresss() {
        // given
        List<ShipmentAddressDTO> shipmentAddresssSavedInDatabase = shipmentAddresssSavedInDatabase();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddresssUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(shipmentAddresssSavedInDatabase.size());
        assertThat(response.getBody())
                .containsOnly(shipmentAddresssSavedInDatabase.toArray(new ShipmentAddressDTO[0]));
    }

    @Test
    void shouldRetrieveOneShipmentAddress() {
        // given
        List<ShipmentAddressDTO> shipmentAddresssSavedInDatabase = shipmentAddresssSavedInDatabase();
        ShipmentAddressDTO shipmentAddressToRetrieve = shipmentAddresssSavedInDatabase.get(2);

        // when
        ResponseEntity<ShipmentAddressDTO> response = restTemplate.exchange(getShipmentAddresssUrl() + "/" + shipmentAddressToRetrieve.getId(), HttpMethod.GET,
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
        ResponseEntity<ShipmentAddressDTO> response = restTemplate.exchange(getShipmentAddresssUrl() + "/999", HttpMethod.GET,
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
        List<ShipmentAddressDTO> shipmentAddresssSavedInDatabase = shipmentAddresssSavedInDatabase();
        ShipmentAddressDTO shipmentAddressToSearch = shipmentAddresssSavedInDatabase.get(2);
        SearchShipmentAddressQuery searchShipmentAddressQuery = SearchShipmentAddressQuery.builder()
                .firstName(shipmentAddressToSearch.getFirstName())
                .build();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddresssUrl() + "/search", HttpMethod.POST,
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
        List<ShipmentAddressDTO> shipmentAddresssSavedInDatabase = shipmentAddresssSavedInDatabase();
        SearchShipmentAddressQuery searchShipmentAddressQuery = SearchShipmentAddressQuery.builder()
                .phone("67")
                .build();

        // when
        ResponseEntity<List<ShipmentAddressDTO>> response = restTemplate.exchange(getShipmentAddresssUrl() + "/search", HttpMethod.POST,
                new HttpEntity<>(searchShipmentAddressQuery),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsOnly(shipmentAddresssSavedInDatabase.get(1), shipmentAddresssSavedInDatabase.get(2), shipmentAddresssSavedInDatabase.get(4));
    }

    private @NotNull String getShipmentAddresssUrl() {
        return getBaseUrl() + "/shipmentAddresss";
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.cloud.aws.region.static", LOCAL_STACK_CONTAINER::getRegion);
        registry.add("spring.cloud.aws.sns.endpoint", () -> LOCAL_STACK_CONTAINER.getEndpointOverride(SNS));
        registry.add("spring.cloud.aws.sns.region", LOCAL_STACK_CONTAINER::getRegion);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> LOCAL_STACK_CONTAINER.getEndpointOverride(SQS));
        registry.add("spring.cloud.aws.sqs.region", LOCAL_STACK_CONTAINER::getRegion);
    }

    private List<ShipmentAddressDTO> shipmentAddresssSavedInDatabase() {
        return shipmentAddressRepository.saveAll(createShipmentAddresss());
    }

    private static List<ShipmentAddressDTO> createShipmentAddresss() {
        return List.of(
                createShipmentAddress("alice@example.com", "Alice", "Smith", "123-456-7890"),
                createShipmentAddress("bob@example.com", "Bob", "Johnson", "234-567-8901",
                        createAddress("123 Main St", "Springfield", "IL", "12345", "USA"),
                        createAddress("234 Elm St", "Springfield", "IL", "23456", "USA")
                ),
                createShipmentAddress("carol@example.com", "Carol", "Williams", "345-678-9012"),
                createShipmentAddress("dave@example.com", "Dave", "Brown", "456-789-0123",
                        createAddress("345 Oak St", "Springfield", "IL", "34567", "USA"),
                        createAddress("456 Pine St", "Springfield", "IL", "45678", "USA")
                ),
                createShipmentAddress("eve@example.com", "Eve", "Jones", "567-890-1234"));
    }

    private static ShipmentAddressDTO createShipmentAddress(String email, String firstName, String lastName, String phone, ShipmentAddressDTO homeAddress, ShipmentAddressDTO deliveryAddress) {
        ShipmentAddressDTO shipmentAddressDTO = createShipmentAddress(email, firstName, lastName, phone);
//        shipmentAddressDTO.setHomeAddress(homeAddress);
//        shipmentAddressDTO.setDeliveryAddress(deliveryAddress);
        return shipmentAddressDTO;
    }

    private static ShipmentAddressDTO createShipmentAddress(String email, String firstName, String lastName, String phone) {
        ShipmentAddressDTO ShipmentAddressDTO = new ShipmentAddressDTO();
        ShipmentAddressDTO.setEmail(email);
        ShipmentAddressDTO.setFirstName(firstName);
        ShipmentAddressDTO.setLastName(lastName);
        ShipmentAddressDTO.setPhone(phone);
        return ShipmentAddressDTO;
    }

    private static ShipmentAddressDTO createAddress(String street, String city, String state, String zipCode, String country) {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO();
        shipmentAddressDTO.setStreet(street);
        shipmentAddressDTO.setCity(city);
        shipmentAddressDTO.setState(state);
        shipmentAddressDTO.setZipCode(zipCode);
        shipmentAddressDTO.setCountry(country);
        return shipmentAddressDTO;
    }

    @SneakyThrows
    private void assertThatEventWasPublished(ShipmentAddressDTO shipmentAddressDTO) {
//        await()
//                .atMost(5, TimeUnit.SECONDS)
//                .untilAsserted(() -> {
//                    ShipmentAddressCreatedEvent receivedShipmentAddressCreatedEvent = receiveOneEvent(getShipmentAddressEventsQueueUrl(), ShipmentAddressCreatedEvent.class);
//
//                    Assertions.assertThat(receivedShipmentAddressCreatedEvent.getTimestamp())
//                            .isNotNull();
//                    Assertions.assertThat(receivedShipmentAddressCreatedEvent.getShipmentAddressDTO())
//                            .isEqualTo(shipmentAddressDTO);
//                });
    }

    private String getShipmentAddressEventsQueueUrl() throws InterruptedException, ExecutionException {
        return amazonSQS.getQueueUrl(GetQueueUrlRequest.builder().queueName("shipmentAddress-events").build()).get().queueUrl();
    }

    private <T> T receiveOneEvent(String queueUrl, Class<T> valueType) throws InterruptedException, ExecutionException, JsonProcessingException {
        List<Message> messages = amazonSQS.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).get().messages();
        assertThat(messages)
                .hasSize(1);

        String messageBody = objectMapper.readTree(messages.getFirst().body()).get("Message").asText();
        return objectMapper.readValue(messageBody, valueType);
    }
}
