package com.dominikcebula.aws.samples.spring.cloud.shared.events.data;

import lombok.Data;

@Data
public class CustomerEventData {
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private AddressEventData homeAddress;
    private AddressEventData deliveryAddress;
}
