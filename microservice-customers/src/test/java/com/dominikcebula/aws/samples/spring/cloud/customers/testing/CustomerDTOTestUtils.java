package com.dominikcebula.aws.samples.spring.cloud.customers.testing;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;

public class CustomerDTOTestUtils {
    public static CustomerDTO copyOfWithId(Long id, CustomerDTO customerUpdateData) {
        CustomerDTO customerDTO = new CustomerDTO(customerUpdateData);
        customerDTO.setId(id);
        return customerDTO;
    }

    public static CustomerDTO copyOfWithoutId(CustomerDTO source) {
        CustomerDTO destination = new CustomerDTO(source);
        destination.setId(null);
        return destination;
    }
}
