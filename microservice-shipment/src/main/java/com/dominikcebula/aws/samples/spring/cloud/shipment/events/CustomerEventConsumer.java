package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerDeletedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CustomerEventConsumer implements Consumer<CustomerEvent> {
    private final CustomerCreatedEventConsumer customerCreatedEventConsumer;
    private final CustomerUpdatedEventConsumer customerUpdatedEventConsumer;
    private final CustomerDeletedEventConsumer customerDeletedEventConsumer;

    @Override
    public void accept(CustomerEvent customerEvent) {
        switch (customerEvent) {
            case CustomerCreatedEvent customerCreatedEvent ->
                    customerCreatedEventConsumer.consume(customerCreatedEvent);
            case CustomerUpdatedEvent customerUpdatedEvent ->
                    customerUpdatedEventConsumer.consume(customerUpdatedEvent);
            case CustomerDeletedEvent customerDeletedEvent ->
                    customerDeletedEventConsumer.consume(customerDeletedEvent);
            default -> throw new IllegalArgumentException("Unable to find event consumer for " + customerEvent);
        }
    }
}
