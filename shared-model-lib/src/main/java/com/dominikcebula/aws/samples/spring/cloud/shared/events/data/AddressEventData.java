package com.dominikcebula.aws.samples.spring.cloud.shared.events.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressEventData {
    private Long addressId;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
