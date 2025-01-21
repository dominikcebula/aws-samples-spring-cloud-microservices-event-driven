package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerCreatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerCreatedEventConsumer {
    private final ModelMapper modelMapper;
    private final ShipmentAddressRepository shipmentAddressRepository;

    @Transactional
    public void consume(CustomerCreatedEvent customerCreatedEvent) {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO();
        modelMapper.map(customerCreatedEvent.getCustomerEventData(), shipmentAddressDTO);
        modelMapper.map(customerCreatedEvent.getCustomerEventData().getDeliveryAddress(), shipmentAddressDTO);

        shipmentAddressRepository.save(shipmentAddressDTO);
    }
}
