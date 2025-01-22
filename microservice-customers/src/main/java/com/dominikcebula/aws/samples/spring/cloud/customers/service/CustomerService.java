package com.dominikcebula.aws.samples.spring.cloud.customers.service;

import com.dominikcebula.aws.samples.spring.cloud.customers.events.CustomerEventsFactory;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.QCustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.dominikcebula.aws.samples.spring.cloud.customers.utils.PredicateUtils.condition;
import static com.querydsl.core.types.ExpressionUtils.allOf;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final EntityManager entityManager;
    private final CustomerEventsFactory customerEventsFactory;
    private final ApplicationEventPublisher eventPublisher;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<CustomerDTO> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public List<CustomerDTO> searchCustomers(SearchCustomerQuery searchCustomerQuery) {
        return new JPAQuery<Void>(entityManager)
                .select(QCustomerDTO.customerDTO)
                .from(QCustomerDTO.customerDTO)
                .where(allOf(
                        condition(searchCustomerQuery.firstName, QCustomerDTO.customerDTO.firstName::containsIgnoreCase),
                        condition(searchCustomerQuery.lastName, QCustomerDTO.customerDTO.lastName::containsIgnoreCase),
                        condition(searchCustomerQuery.email, QCustomerDTO.customerDTO.email::containsIgnoreCase),
                        condition(searchCustomerQuery.phone, QCustomerDTO.customerDTO.phone::containsIgnoreCase)
                )).fetch();
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customer) {
        CustomerDTO createdCustomer = customerRepository.save(customer);
        eventPublisher.publishEvent(customerEventsFactory.createCustomerCreatedEvent(createdCustomer));
        return createdCustomer;
    }

    @Transactional
    public UpdateCustomerResultData updateCustomer(Long id, CustomerDTO customerUpdateData) {
        return customerRepository.findById(id)
                .map(foundCustomer -> {
                    CustomerDTO oldCustomer = new CustomerDTO(foundCustomer);
                    customerUpdateData.setId(foundCustomer.getId());
                    CustomerDTO savedCustomer = customerRepository.save(customerUpdateData);
                    eventPublisher.publishEvent(customerEventsFactory.createCustomerUpdatedEvent(oldCustomer, savedCustomer));
                    return new UpdateCustomerResultData(UpdateCustomerResult.UPDATED, savedCustomer);
                })
                .orElse(new UpdateCustomerResultData(UpdateCustomerResult.NOT_FOUND));
    }

    @Transactional
    public DeleteCustomerResult deleteCustomer(Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customerRepository.deleteById(customer.getId());
                    eventPublisher.publishEvent(customerEventsFactory.createCustomerDeletedEvent(customer));
                    return DeleteCustomerResult.DELETED;
                })
                .orElse(DeleteCustomerResult.NOT_FOUND);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchCustomerQuery {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UpdateCustomerResultData {
        private final UpdateCustomerResult updateCustomerResult;
        private CustomerDTO savedCustomer;
    }

    public enum UpdateCustomerResult {
        UPDATED,
        NOT_FOUND
    }

    public enum DeleteCustomerResult {
        DELETED,
        NOT_FOUND
    }
}
