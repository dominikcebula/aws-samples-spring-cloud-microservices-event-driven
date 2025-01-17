package com.dominikcebula.aws.samples.spring.cloud.shipment.model;

import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "shipment_addresses")
@Data
public class ShipmentAddressDTO {
    @Id
    @SequenceGenerator(name = "shipment_addresses_seq", sequenceName = "shipment_addresses_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "shipment_addresses_seq")
    private Long id;

    private Long customerId;
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
