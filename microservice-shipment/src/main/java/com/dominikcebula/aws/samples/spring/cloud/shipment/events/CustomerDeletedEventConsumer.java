package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerDeletedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerDeletedEventConsumer {
    private final ShipmentAddressRepository shipmentAddressRepository;

    @Transactional
    public void consume(CustomerDeletedEvent customerDeletedEvent) {
        shipmentAddressRepository.deleteById(customerDeletedEvent.getCustomerEventData().getDeliveryAddress().getAddressId());
    }
}
