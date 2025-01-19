package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEventType.CREATED;

@Service
@RequiredArgsConstructor
public class CustomerCreatedEventFactory {
    private final ModelMapper modelMapper;

    public CustomerEvent createCustomerCreatedEvent(CustomerDTO customer) {
        CustomerEventData customerEventData = modelMapper.map(customer, CustomerEventData.class);

        return new CustomerEvent(CREATED, customerEventData);
    }
}
