package com.dominikcebula.aws.samples.spring.cloud.customers.service;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<CustomerDTO> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public CustomerDTO createCustomer(CustomerDTO customer) {
        return customerRepository.save(customer);
    }

    public UpdateCustomerResultData updateCustomer(Long id, CustomerDTO customer) {
        if (customerRepository.existsById(id)) {
            customer.setId(id);
            CustomerDTO savedCustomer = customerRepository.save(customer);
            return new UpdateCustomerResultData(UpdateCustomerResult.UPDATED, savedCustomer);
        } else
            return new UpdateCustomerResultData(UpdateCustomerResult.NOT_FOUND);
    }

    public DeleteCustomerResult deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return DeleteCustomerResult.DELETED;
        } else
            return DeleteCustomerResult.NOT_FOUND;
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
