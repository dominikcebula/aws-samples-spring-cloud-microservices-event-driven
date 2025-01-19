package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerEventsListener {
    private final EventSender eventSender;

    @EventListener
    public void onCustomerCreatedEvent(CustomerEvent event) {
        eventSender.send("customerEvents-out-0", event);
    }
}
