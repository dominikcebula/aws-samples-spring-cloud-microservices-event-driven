package com.dominikcebula.aws.samples.spring.cloud.customers.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
public class CustomerDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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
