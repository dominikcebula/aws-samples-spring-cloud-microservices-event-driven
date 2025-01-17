package com.dominikcebula.aws.samples.spring.cloud.shipment.repository;

import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentAddressRepository extends ListCrudRepository<ShipmentAddressDTO, Long> {
}
