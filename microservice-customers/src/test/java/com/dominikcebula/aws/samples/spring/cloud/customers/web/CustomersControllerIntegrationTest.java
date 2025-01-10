package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "local"})
class CustomersControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17");

    private static final List<CustomerDTO> CUSTOMERS = createCustomers();

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
    @DirtiesContext
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
    @DirtiesContext
    void shouldRetrieveCustomers() {
        // given
        customersSavedInDatabase();

        // when
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(getCustomersUrl(), HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(CUSTOMERS.size());
        assertThat(response.getBody())
                .containsOnly(CUSTOMERS.toArray(new CustomerDTO[0]));
    }

    @Test
    @DirtiesContext
    void shouldRetrieveOneCustomer() {
        // given
        customersSavedInDatabase();
        CustomerDTO customerToRetrieve = CUSTOMERS.get(2);

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
    @DirtiesContext
    void shouldUpdateOneCustomer() {
        // given
        customersSavedInDatabase();
        CustomerDTO customerToUpdate = CUSTOMERS.get(3);
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
                .containsOnly(CUSTOMERS.get(0), CUSTOMERS.get(1), CUSTOMERS.get(2), customerToUpdate, CUSTOMERS.get(4));
    }

    @Test
    void shouldNotUpdateNonExistingCustomer() {
        // given
        CustomerDTO customerToUpdate = CUSTOMERS.get(3);

        // when
        ResponseEntity<?> response = restTemplate.exchange(getCustomersUrl() + "/999", HttpMethod.PUT, new HttpEntity<>(customerToUpdate), new ParameterizedTypeReference<>() {
        });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteOneCustomer() {
        // given
        customersSavedInDatabase();
        CustomerDTO customerToDelete = CUSTOMERS.get(3);

        // when
        ResponseEntity<?> response = restTemplate.exchange(getCustomersUrl() + "/" + customerToDelete.getId(), HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {
                });

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerRepository.findAll())
                .containsOnly(CUSTOMERS.get(0), CUSTOMERS.get(1), CUSTOMERS.get(2), CUSTOMERS.get(4));
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

    private static List<CustomerDTO> createCustomers() {
        return List.of(
                createCustomer(1L, "alice@example.com", "Alice", "Smith", "123-456-7890"),
                createCustomer(2L, "bob@example.com", "Bob", "Johnson", "234-567-8901"),
                createCustomer(3L, "carol@example.com", "Carol", "Williams", "345-678-9012"),
                createCustomer(4L, "dave@example.com", "Dave", "Brown", "456-789-0123"),
                createCustomer(5L, "eve@example.com", "Eve", "Jones", "567-890-1234"));
    }

    private static CustomerDTO createCustomer(Long id, String email, String firstName, String lastName, String phone) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(id);
        customerDTO.setEmail(email);
        customerDTO.setFirstName(firstName);
        customerDTO.setLastName(lastName);
        customerDTO.setPhone(phone);
        return customerDTO;
    }

    private void customersSavedInDatabase() {
        customerRepository.saveAll(CUSTOMERS);
    }
}
