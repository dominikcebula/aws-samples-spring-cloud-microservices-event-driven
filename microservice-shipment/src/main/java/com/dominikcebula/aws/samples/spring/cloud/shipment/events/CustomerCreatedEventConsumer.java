package com.dominikcebula.aws.samples.spring.cloud.shipment.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.CustomerEvent;
import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerCreatedEventConsumer {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ShipmentAddressRepository shipmentAddressRepository;

    public void consume(CustomerEvent customerEvent) {
        ShipmentAddressDTO shipmentAddressDTO = new ShipmentAddressDTO();
        modelMapper.map(customerEvent.getCustomerEventData(), shipmentAddressDTO);
        modelMapper.map(customerEvent.getCustomerEventData().getDeliveryAddress(), shipmentAddressDTO);

        shipmentAddressRepository.save(shipmentAddressDTO);
    }
}
