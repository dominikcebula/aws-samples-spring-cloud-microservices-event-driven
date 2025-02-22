package com.dominikcebula.aws.samples.spring.cloud.shared.events.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEventData {
    private Long customerId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private AddressEventData homeAddress;
    private AddressEventData deliveryAddress;
}
