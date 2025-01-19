package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CustomerEventConsumer implements Consumer<CustomerEvent> {
    private final CustomerCreatedEventConsumer customerCreatedEventConsumer;
    private final CustomerDeletedEventConsumer customerDeletedEventConsumer;

    @Override
    public void accept(CustomerEvent customerEvent) {
        switch (customerEvent.getEventType()) {
            case CREATED -> customerCreatedEventConsumer.consume(customerEvent);
            case DELETED -> customerDeletedEventConsumer.consume(customerEvent);
            default -> throw new IllegalArgumentException("Unable to find event consumer for " + customerEvent);
        }
    }
}
