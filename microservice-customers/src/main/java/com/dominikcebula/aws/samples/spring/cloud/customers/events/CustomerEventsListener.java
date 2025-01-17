package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventsListener {
    @Autowired
    private EventSender eventSender;

    @EventListener
    public void onCustomerCreatedEvent(CustomerCreatedEvent event) {
        eventSender.send("customerEvents-out-0", event);
    }
}
