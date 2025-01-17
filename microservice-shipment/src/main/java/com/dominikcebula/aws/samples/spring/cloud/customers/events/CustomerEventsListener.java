package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventsListener {
    @Autowired
    private StreamBridge streamBridge;

    @EventListener
    public void onCustomerCreatedEvent(CustomerCreatedEvent event) {
        streamBridge.send("customerEvents-out-0", event);
    }
}
