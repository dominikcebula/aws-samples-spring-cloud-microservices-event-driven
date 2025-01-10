package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.AddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.SearchCustomerQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

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

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17");

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
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customerDTO);
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
}
