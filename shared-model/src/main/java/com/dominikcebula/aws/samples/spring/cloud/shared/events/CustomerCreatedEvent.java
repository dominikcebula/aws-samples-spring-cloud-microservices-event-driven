package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CustomerCreatedEvent extends Event {
    private CustomerEventData customerEventData;

    public CustomerCreatedEvent(CustomerEventData customerEventData) {
        super(ZonedDateTime.now());
        this.customerEventData = customerEventData;
    }
}
