package com.dominikcebula.aws.samples.spring.cloud.shipment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipment_addresses")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShipmentAddressDTO {
    @Id
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
