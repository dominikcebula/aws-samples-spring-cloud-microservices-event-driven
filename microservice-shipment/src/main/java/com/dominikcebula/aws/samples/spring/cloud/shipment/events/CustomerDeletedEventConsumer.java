package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerDeletedEventConsumer {
    @Autowired
    private ShipmentAddressRepository shipmentAddressRepository;

    public void consume(CustomerEvent customerEvent) {
        shipmentAddressRepository.deleteById(customerEvent.getCustomerEventData().getDeliveryAddress().getId());
    }
}
