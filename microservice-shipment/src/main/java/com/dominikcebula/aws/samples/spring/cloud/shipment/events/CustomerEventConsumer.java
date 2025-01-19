package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CustomerEventConsumer implements Consumer<CustomerEvent> {
    @Autowired
    private CustomerCreatedEventConsumer customerCreatedEventConsumer;
    @Autowired
    private CustomerDeletedEventConsumer customerDeletedEventConsumer;

    @Override
    public void accept(CustomerEvent customerEvent) {
        switch (customerEvent.getEventType()) {
            case CREATED -> customerCreatedEventConsumer.consume(customerEvent);
            case DELETED -> customerDeletedEventConsumer.consume(customerEvent);
            default -> throw new IllegalArgumentException("Unable to find event consumer for " + customerEvent);
        }
    }
}
