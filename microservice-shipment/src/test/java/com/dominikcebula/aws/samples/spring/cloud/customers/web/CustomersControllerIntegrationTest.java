package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import com.dominikcebula.aws.samples.spring.cloud.customers.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.AddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import java.util.concurrent.TimeUnit;

import static com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.SearchCustomerQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class CustomersControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

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
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sns", "create-topic", "--name", "customer-events-topic");
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "customer-events");
        LOCAL_STACK_CONTAINER.execInContainer("awslocal", "sns", "subscribe", "--topic-arn", "arn:aws:sns:us-east-1:000000000000:customer-events-topic", "--protocol", "sqs", "--notification-endpoint", "arn:aws:sqs:us-east-1:000000000000:customer-events");
    }

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyCustomersList() {
        // when
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(getCustomersUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldSaveCustomer() {
        // given
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setEmail("John.Doe@mail.com");
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");
        customerDTO.setPhone("123-456-7890");

        // when
        ResponseEntity<CustomerDTO> response = restTemplate.postForEntity(getCustomersUrl(), customerDTO, CustomerDTO.class);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders())
                .containsEntry("Location", List.of("/api/v1/customers/1"));
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getId())
                .isNotNull();
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customerDTO);
        assertThatEventWasPublished(response.getBody());
    }

    @Test
    void shouldRetrieveCustomers() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();

        // when
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(getCustomersUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(customersSavedInDatabase.size());
        assertThat(response.getBody())
                .containsOnly(customersSavedInDatabase.toArray(new CustomerDTO[0]));
    }

    @Test
    void shouldRetrieveOneCustomer() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToRetrieve = customersSavedInDatabase.get(2);

        // when
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(getCustomersUrl() + "/" + customerToRetrieve.getId(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(customerToRetrieve);
    }

    @Test
    void shouldNotRetrieveNonExistingCustomer() {
        // when
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(getCustomersUrl() + "/999", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldSearchCustomerByName() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToSearch = customersSavedInDatabase.get(2);
        SearchCustomerQuery searchCustomerQuery = SearchCustomerQuery.builder()
                .firstName(customerToSearch.getFirstName())
                .build();

        // when
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(getCustomersUrl() + "/search", HttpMethod.POST,
                new HttpEntity<>(searchCustomerQuery),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsOnly(customerToSearch);
    }

    @Test
    void shouldSearchCustomerByPhone() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        SearchCustomerQuery searchCustomerQuery = SearchCustomerQuery.builder()
                .phone("67")
                .build();

        // when
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(getCustomersUrl() + "/search", HttpMethod.POST,
                new HttpEntity<>(searchCustomerQuery),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsOnly(customersSavedInDatabase.get(1), customersSavedInDatabase.get(2), customersSavedInDatabase.get(4));
    }

    @Test
    void shouldUpdateOneCustomer() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToUpdate = customersSavedInDatabase.get(3);
        customerToUpdate.setFirstName("Greg");
        customerToUpdate.setLastName("Brown");

        // when
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(getCustomersUrl() + "/" + customerToUpdate.getId(), HttpMethod.PUT,
                new HttpEntity<>(customerToUpdate),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(customerToUpdate);
        assertThat(customerRepository.findAll())
                .containsOnly(
                        customersSavedInDatabase.get(0),
                        customersSavedInDatabase.get(1),
                        customersSavedInDatabase.get(2),
                        customerToUpdate,
                        customersSavedInDatabase.get(4));
    }

    @Test
    void shouldNotUpdateNonExistingCustomer() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToUpdate = customersSavedInDatabase.get(3);

        // when
        ResponseEntity<?> response = restTemplate.exchange(getCustomersUrl() + "/999", HttpMethod.PUT, new HttpEntity<>(customerToUpdate), new ParameterizedTypeReference<>() {
        });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteOneCustomer() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToDelete = customersSavedInDatabase.get(3);

        // when
        ResponseEntity<?> response = restTemplate.exchange(getCustomersUrl() + "/" + customerToDelete.getId(), HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerRepository.findAll())
                .containsOnly(customersSavedInDatabase.get(0),
                        customersSavedInDatabase.get(1),
                        customersSavedInDatabase.get(2),
                        customersSavedInDatabase.get(4));
    }

    @Test
    void shouldNotDeleteNonExistingCustomer() {
        // when
        ResponseEntity<Object> response = restTemplate.exchange(getCustomersUrl() + "/999", HttpMethod.DELETE, null, new ParameterizedTypeReference<>() {
        });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private @NotNull String getCustomersUrl() {
        return getBaseUrl() + "/customers";
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

    private List<CustomerDTO> customersSavedInDatabase() {
        return customerRepository.saveAll(createCustomers());
    }

    private static List<CustomerDTO> createCustomers() {
        return List.of(
                createCustomer("alice@example.com", "Alice", "Smith", "123-456-7890"),
                createCustomer("bob@example.com", "Bob", "Johnson", "234-567-8901",
                        createAddress("123 Main St", "Springfield", "IL", "12345", "USA"),
                        createAddress("234 Elm St", "Springfield", "IL", "23456", "USA")
                ),
                createCustomer("carol@example.com", "Carol", "Williams", "345-678-9012"),
                createCustomer("dave@example.com", "Dave", "Brown", "456-789-0123",
                        createAddress("345 Oak St", "Springfield", "IL", "34567", "USA"),
                        createAddress("456 Pine St", "Springfield", "IL", "45678", "USA")
                ),
                createCustomer("eve@example.com", "Eve", "Jones", "567-890-1234"));
    }

    private static CustomerDTO createCustomer(String email, String firstName, String lastName, String phone, AddressDTO homeAddress, AddressDTO deliveryAddress) {
        CustomerDTO customerDTO = createCustomer(email, firstName, lastName, phone);
        customerDTO.setHomeAddress(homeAddress);
        customerDTO.setDeliveryAddress(deliveryAddress);
        return customerDTO;
    }

    private static CustomerDTO createCustomer(String email, String firstName, String lastName, String phone) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setEmail(email);
        customerDTO.setFirstName(firstName);
        customerDTO.setLastName(lastName);
        customerDTO.setPhone(phone);
        return customerDTO;
    }

    private static AddressDTO createAddress(String street, String city, String state, String zipCode, String country) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet(street);
        addressDTO.setCity(city);
        addressDTO.setState(state);
        addressDTO.setZipCode(zipCode);
        addressDTO.setCountry(country);
        return addressDTO;
    }

    @SneakyThrows
    private void assertThatEventWasPublished(CustomerDTO customerDTO) {
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CustomerCreatedEvent receivedCustomerCreatedEvent = receiveOneEvent(getCustomerEventsQueueUrl(), CustomerCreatedEvent.class);

                    assertThat(receivedCustomerCreatedEvent.getTimestamp())
                            .isNotNull();
                    assertThat(receivedCustomerCreatedEvent.getCustomerDTO())
                            .isEqualTo(customerDTO);
                });
    }

    private String getCustomerEventsQueueUrl() throws InterruptedException, ExecutionException {
        return amazonSQS.getQueueUrl(GetQueueUrlRequest.builder().queueName("customer-events").build()).get().queueUrl();
    }

    private <T> T receiveOneEvent(String queueUrl, Class<T> valueType) throws InterruptedException, ExecutionException, JsonProcessingException {
        List<Message> messages = amazonSQS.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).get().messages();
        assertThat(messages)
                .hasSize(1);

        String messageBody = objectMapper.readTree(messages.getFirst().body()).get("Message").asText();
        return objectMapper.readValue(messageBody, valueType);
    }
}
