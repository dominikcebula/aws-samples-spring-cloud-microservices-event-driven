package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerEventsFactory {
    private final ModelMapper modelMapper;

    public CustomerCreatedEvent createCustomerCreatedEvent(CustomerDTO customer) {
        CustomerEventData customerEventData = modelMapper.map(customer, CustomerEventData.class);

        return new CustomerCreatedEvent(customerEventData);
    }
}
