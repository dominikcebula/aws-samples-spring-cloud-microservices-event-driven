package com.dominikcebula.aws.samples.spring.cloud.customers.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
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

    public AddressDTO(AddressDTO other) {
        this.id = other.getId();
        this.street = other.getStreet();
        this.city = other.getCity();
        this.state = other.getState();
        this.zipCode = other.getZipCode();
        this.country = other.getCountry();
    }
}
