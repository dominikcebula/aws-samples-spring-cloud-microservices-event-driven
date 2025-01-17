package com.dominikcebula.aws.samples.spring.cloud.shipment.web;

import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.service.ShipmentAddressService;
import com.dominikcebula.aws.samples.spring.cloud.shipment.service.ShipmentAddressService.SearchShipmentAddressQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShipmentAddressController {
    @Autowired
    private ShipmentAddressService shipmentAddressService;

    @GetMapping("/shipment/addresses")
    public List<ShipmentAddressDTO> getAllShipmentAddresses() {
        return shipmentAddressService.getAllShipmentAddresses();
    }

    @GetMapping("/shipment/addresses/{id}")
    public ResponseEntity<ShipmentAddressDTO> getShipmentAddressesById(@PathVariable("id") Long id) {
        return shipmentAddressService.getShipmentAddressesById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/shipment/addresses/search")
    public ResponseEntity<List<ShipmentAddressDTO>> searchShipmentAddress(@RequestBody SearchShipmentAddressQuery searchShipmentAddressQuery) {
        return ResponseEntity
                .ok()
                .body(shipmentAddressService.searchShipmentAddress(searchShipmentAddressQuery));
    }
}
