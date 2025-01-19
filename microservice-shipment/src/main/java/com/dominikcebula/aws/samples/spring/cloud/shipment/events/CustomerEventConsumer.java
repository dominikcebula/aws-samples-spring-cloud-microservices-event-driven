package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CustomerEventConsumer implements Consumer<CustomerEvent> {
    @Override
    public void accept(CustomerEvent customerEvent) {
        System.out.println("Customer created: " + customerEvent);
    }
}
