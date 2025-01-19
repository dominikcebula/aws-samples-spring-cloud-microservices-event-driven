package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CustomerEvent extends Event {
    private CustomerEventType eventType;
    private CustomerEventData customerEventData;

    public CustomerEvent(CustomerEventType eventType, CustomerEventData customerEventData) {
        super(ZonedDateTime.now());
        this.eventType = eventType;
        this.customerEventData = customerEventData;
    }
}
