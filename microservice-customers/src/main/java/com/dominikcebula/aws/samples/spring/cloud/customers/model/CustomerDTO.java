package com.dominikcebula.aws.samples.spring.cloud.customers.model;

import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "customers")
@Data
public class CustomerDTO {
    @Id
    @SequenceGenerator(name = "customers_seq", sequenceName = "customers_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "customers_seq")
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @OneToOne(cascade = CascadeType.ALL)
    private AddressDTO homeAddress;
    @OneToOne(cascade = CascadeType.ALL)
    private AddressDTO deliveryAddress;
}
