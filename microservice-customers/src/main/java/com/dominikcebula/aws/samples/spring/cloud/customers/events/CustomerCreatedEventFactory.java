package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerCreatedEventFactory {
    @Autowired
    private ModelMapper modelMapper;

    public CustomerCreatedEvent createCustomerCreatedEvent(CustomerDTO customer) {
        CustomerEventData customerEventData = modelMapper.map(customer, CustomerEventData.class);

        return new CustomerCreatedEvent(customerEventData);
    }
}
