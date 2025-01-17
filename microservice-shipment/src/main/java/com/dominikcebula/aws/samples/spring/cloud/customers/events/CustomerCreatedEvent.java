package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class CustomerCreatedEvent extends Event {
    private CustomerDTO customerDTO;

    public CustomerCreatedEvent(CustomerDTO customerDTO) {
        super(ZonedDateTime.now());
        this.customerDTO = customerDTO;
    }
}
