package com.dominikcebula.aws.samples.spring.cloud.shipment.service;

import com.dominikcebula.aws.samples.spring.cloud.shipment.model.QShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.model.ShipmentAddressDTO;
import com.dominikcebula.aws.samples.spring.cloud.shipment.repository.ShipmentAddressRepository;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.dominikcebula.aws.samples.spring.cloud.shipment.utils.PredicateUtils.condition;
import static com.querydsl.core.types.ExpressionUtils.allOf;

@Service
@RequiredArgsConstructor
public class ShipmentAddressService {
    private final ShipmentAddressRepository shipmentAddressRepository;
    private final EntityManager entityManager;

    public List<ShipmentAddressDTO> getAllShipmentAddresses() {
        return shipmentAddressRepository.findAll();
    }

    public Optional<ShipmentAddressDTO> getShipmentAddressesById(Long id) {
        return shipmentAddressRepository.findById(id);
    }

    public List<ShipmentAddressDTO> searchShipmentAddress(SearchShipmentAddressQuery searchShipmentAddressQuery) {
        return new JPAQuery<Void>(entityManager)
                .select(QShipmentAddressDTO.shipmentAddressDTO)
                .from(QShipmentAddressDTO.shipmentAddressDTO)
                .where(allOf(
                        condition(searchShipmentAddressQuery.customerId, QShipmentAddressDTO.shipmentAddressDTO.customerId::eq),
                        condition(searchShipmentAddressQuery.addressId, QShipmentAddressDTO.shipmentAddressDTO.addressId::eq),
                        condition(searchShipmentAddressQuery.firstName, QShipmentAddressDTO.shipmentAddressDTO.firstName::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.lastName, QShipmentAddressDTO.shipmentAddressDTO.lastName::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.email, QShipmentAddressDTO.shipmentAddressDTO.email::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.phone, QShipmentAddressDTO.shipmentAddressDTO.phone::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.street, QShipmentAddressDTO.shipmentAddressDTO.street::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.city, QShipmentAddressDTO.shipmentAddressDTO.city::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.state, QShipmentAddressDTO.shipmentAddressDTO.state::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.zipCode, QShipmentAddressDTO.shipmentAddressDTO.zipCode::containsIgnoreCase),
                        condition(searchShipmentAddressQuery.country, QShipmentAddressDTO.shipmentAddressDTO.country::containsIgnoreCase)
                )).fetch();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchShipmentAddressQuery {
        private Long customerId;
        private Long addressId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
