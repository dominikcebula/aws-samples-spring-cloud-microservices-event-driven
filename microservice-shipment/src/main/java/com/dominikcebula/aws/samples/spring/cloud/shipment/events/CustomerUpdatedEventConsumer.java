package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerUpdatedEvent;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.AddressEventData;
import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerUpdatedEventConsumer {
    private final ModelMapper modelMapper;
    private final ShipmentAddressRepository shipmentAddressRepository;

    @Transactional
    public void consume(CustomerUpdatedEvent customerUpdatedEvent) {
        CustomerEventData customerEventData = customerUpdatedEvent.getNewCustomerData();
        AddressEventData deliveryAddressEventData = customerEventData.getDeliveryAddress();

        ShipmentAddressDTO shipmentAddressDTO = shipmentAddressRepository.findById(deliveryAddressEventData.getAddressId())
                .orElseThrow(() -> new IllegalStateException("No address found for address id: " + deliveryAddressEventData.getAddressId()));

        modelMapper.map(customerEventData, shipmentAddressDTO);
        modelMapper.map(deliveryAddressEventData, shipmentAddressDTO);
    }
}
