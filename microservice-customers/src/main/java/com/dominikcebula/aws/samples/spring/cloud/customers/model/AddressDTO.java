package com.dominikcebula.aws.samples.spring.cloud.customers.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "addresses")
@Data
public class AddressDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
