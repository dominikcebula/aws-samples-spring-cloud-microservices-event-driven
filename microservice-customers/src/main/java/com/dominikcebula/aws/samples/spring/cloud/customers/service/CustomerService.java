package com.dominikcebula.aws.samples.spring.cloud.customers.service;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll();
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }


    public CustomerDTO createCustomer(CustomerDTO customer) {
        return customerRepository.save(customer);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customer) {
        if (customerRepository.existsById(id)) {
            customer.setId(id);
            return customerRepository.save(customer);
        }
        return null;
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
