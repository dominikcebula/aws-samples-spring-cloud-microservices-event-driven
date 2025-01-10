package com.dominikcebula.aws.samples.spring.cloud.customers.model;

import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "addresses")
@Data
public class AddressDTO {
    @Id
    @SequenceGenerator(name = "addresses_seq", sequenceName = "addresses_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "addresses_seq")
    private Long id;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
