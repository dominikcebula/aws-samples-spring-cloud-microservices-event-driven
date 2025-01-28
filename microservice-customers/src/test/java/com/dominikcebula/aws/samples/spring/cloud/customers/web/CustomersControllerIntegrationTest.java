package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.AddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerDeletedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerUpdatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import com.dominikcebula.aws.samples.spring.cloud.testing.DefaultLocalStackCredentialsConfiguration;
import com.dominikcebula.aws.samples.spring.cloud.testing.LocalStackContainerSupport;
import com.dominikcebula.aws.samples.spring.cloud.testing.PostgreSQLContainerSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.SearchCustomerQuery;
import static com.dominikcebula.aws.samples.spring.cloud.customers.testing.CustomerDTOTestUtils.copyOfWithId;
import static com.dominikcebula.aws.samples.spring.cloud.customers.testing.CustomerDTOTestUtils.copyOfWithoutId;
import static com.dominikcebula.aws.samples.spring.cloud.testing.LocalStackContainerSupport.QUEUE_CUSTOMER_EVENTS_TO_TEST_CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Import(DefaultLocalStackCredentialsConfiguration.class)
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
    @SpyBean
    private StreamBridge streamBridge;

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
        assertNoEventsPublished();
    }

    @Test
    void shouldSaveCustomer() {
        // given
        CustomerDTO customerDTO = createCustomer("bob@example.com", "Bob", "Johnson", "234-567-8901",
                createAddress("123 Main St", "Springfield", "IL", "12345", "USA"),
                createAddress("234 Elm St", "Springfield", "IL", "23456", "USA")
        );

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
                .ignoringFields("id", "homeAddress.id", "deliveryAddress.id")
                .isEqualTo(customerDTO);
        assertThatCustomerCreatedEventWasPublished(response.getBody());
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
        assertNoEventsPublished();
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
        assertNoEventsPublished();
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
        assertNoEventsPublished();
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
        assertNoEventsPublished();
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
        assertNoEventsPublished();
    }

    @Test
    void shouldUpdateOneCustomer() {
        // given
        List<CustomerDTO> customersSavedInDatabase = customersSavedInDatabase();
        CustomerDTO customerToUpdate = customersSavedInDatabase.get(3);
        CustomerDTO customerUpdateData = copyOfWithoutId(customerToUpdate);
        customerUpdateData.setFirstName("Greg");
        customerUpdateData.setLastName("Brown");

        // when
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(getCustomersUrl() + "/" + customerToUpdate.getId(), HttpMethod.PUT,
                new HttpEntity<>(customerUpdateData),
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull();
        assertThat(response.getBody().getId())
                .isEqualTo(customerToUpdate.getId());
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customerUpdateData);
        assertThat(customerRepository.findAll())
                .containsOnly(
                        customersSavedInDatabase.get(0),
                        customersSavedInDatabase.get(1),
                        customersSavedInDatabase.get(2),
                        copyOfWithId(customerToUpdate.getId(), customerUpdateData),
                        customersSavedInDatabase.get(4));
        assertThatCustomerUpdatedEventWasPublished(customerToUpdate, response.getBody());
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
        assertNoEventsPublished();
    }

    @Test
    void shouldDeleteOneCustomer() throws InterruptedException {
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
        assertThatCustomerDeletedEventWasPublished(customerToDelete);
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
        PostgreSQLContainerSupport.registerProperties(registry);
        LocalStackContainerSupport.registerProperties(registry);
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
    private void assertThatCustomerCreatedEventWasPublished(CustomerDTO customerDTO) {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CustomerCreatedEvent retrievedCustomerEvent = retrieveOneEvent(getCustomerEventsQueueUrl(), CustomerCreatedEvent.class);
                    CustomerEventData retrievedCustomerEventData = retrievedCustomerEvent.getCustomerEventData();

                    assertThat(retrievedCustomerEvent.getTimestamp()).isNotNull();
                    assertEventDataMatches(retrievedCustomerEventData, customerDTO);
                });
    }

    @SneakyThrows
    private void assertThatCustomerUpdatedEventWasPublished(CustomerDTO customerToUpdate, CustomerDTO updatedCustomer) {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CustomerUpdatedEvent retrievedCustomerEvent = retrieveOneEvent(getCustomerEventsQueueUrl(), CustomerUpdatedEvent.class);
                    CustomerEventData oldCustomerEventData = retrievedCustomerEvent.getOldCustomerData();
                    CustomerEventData newCustomerEventData = retrievedCustomerEvent.getNewCustomerData();

                    assertThat(retrievedCustomerEvent.getTimestamp()).isNotNull();
                    assertEventDataMatches(oldCustomerEventData, customerToUpdate);
                    assertEventDataMatches(newCustomerEventData, updatedCustomer);
                });
    }

    private void assertThatCustomerDeletedEventWasPublished(CustomerDTO customerToDelete) {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CustomerDeletedEvent retrievedCustomerEvent = retrieveOneEvent(getCustomerEventsQueueUrl(), CustomerDeletedEvent.class);
                    CustomerEventData customerEventData = retrievedCustomerEvent.getCustomerEventData();

                    assertThat(retrievedCustomerEvent.getTimestamp()).isNotNull();
                    assertEventDataMatches(customerEventData, customerToDelete);
                });
    }

    private void assertEventDataMatches(CustomerEventData customerEventData, CustomerDTO customerData) {
        assertThat(customerEventData.getCustomerId())
                .isEqualTo(customerData.getId());
        assertThat(customerEventData)
                .usingRecursiveComparison()
                .ignoringFields("customerId", "homeAddress.addressId", "deliveryAddress.addressId")
                .isEqualTo(customerData);
        assertThat(customerEventData.getHomeAddress().getAddressId())
                .isEqualTo(customerData.getHomeAddress().getId());
        assertThat(customerEventData.getDeliveryAddress().getAddressId())
                .isEqualTo(customerData.getDeliveryAddress().getId());
    }

    private String getCustomerEventsQueueUrl() throws InterruptedException, ExecutionException {
        return amazonSQS.getQueueUrl(GetQueueUrlRequest.builder().queueName(QUEUE_CUSTOMER_EVENTS_TO_TEST_CONSUMER).build()).get().queueUrl();
    }

    private <T> T retrieveOneEvent(String queueUrl, Class<T> valueType) throws InterruptedException, ExecutionException, JsonProcessingException {
        List<Message> messages = amazonSQS.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).waitTimeSeconds(10).build()).get().messages();
        assertThat(messages)
                .hasSize(1);

        String messageBody = objectMapper.readTree(messages.getFirst().body()).get("Message").asText();
        return objectMapper.readValue(messageBody, valueType);
    }

    private void assertNoEventsPublished() {
        verify(streamBridge, atMostOnce()).afterSingletonsInstantiated();
        verifyNoMoreInteractions(streamBridge);
    }
}
