package com.dominikcebula.aws.samples.spring.cloud.customers.service;

import com.dominikcebula.aws.samples.spring.cloud.customers.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.model.QCustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.dominikcebula.aws.samples.spring.cloud.customers.utils.PredicateUtils.condition;
import static com.querydsl.core.types.ExpressionUtils.allOf;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(new CustomerCreatedEvent(createdCustomer));
        return createdCustomer;
    }

    @Transactional
    public UpdateCustomerResultData updateCustomer(Long id, CustomerDTO customer) {
        if (customerRepository.existsById(id)) {
            customer.setId(id);
            CustomerDTO savedCustomer = customerRepository.save(customer);
            return new UpdateCustomerResultData(UpdateCustomerResult.UPDATED, savedCustomer);
        } else
            return new UpdateCustomerResultData(UpdateCustomerResult.NOT_FOUND);
    }

    @Transactional
    public DeleteCustomerResult deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return DeleteCustomerResult.DELETED;
        } else
            return DeleteCustomerResult.NOT_FOUND;
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
