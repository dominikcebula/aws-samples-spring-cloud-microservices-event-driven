package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService;
import com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.UpdateCustomerResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.DeleteCustomerResult;
import static com.dominikcebula.aws.samples.spring.cloud.customers.service.CustomerService.UpdateCustomerResult;

@RestController
@RequestMapping("/api/v1")
public class CustomersController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customers")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customer) {
        CustomerDTO createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/v1/customers/" + createdCustomer.getId())
                .body(createdCustomer);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customer) {
        UpdateCustomerResultData updateCustomerResultData = customerService.updateCustomer(id, customer);

        if (updateCustomerResultData.getUpdateCustomerResult() == UpdateCustomerResult.UPDATED)
            return ResponseEntity.ok().body(updateCustomerResultData.getSavedCustomer());
        else
            return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        DeleteCustomerResult deleteCustomerResult = customerService.deleteCustomer(id);

        if (deleteCustomerResult == DeleteCustomerResult.DELETED)
            return ResponseEntity.noContent().build();
        else
            return ResponseEntity.notFound().build();
    }
}
