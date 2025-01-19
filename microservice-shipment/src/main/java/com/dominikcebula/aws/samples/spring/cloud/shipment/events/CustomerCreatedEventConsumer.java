package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
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
    public void consume(CustomerEvent customerEvent) {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO();
        modelMapper.map(customerEvent.getCustomerEventData(), shipmentAddressDTO);
        modelMapper.map(customerEvent.getCustomerEventData().getDeliveryAddress(), shipmentAddressDTO);

        shipmentAddressRepository.save(shipmentAddressDTO);
    }
}
