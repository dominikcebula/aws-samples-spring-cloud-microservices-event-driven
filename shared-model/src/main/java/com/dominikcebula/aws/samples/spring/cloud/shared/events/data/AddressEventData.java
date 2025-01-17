package com.dominikcebula.aws.samples.spring.cloud.shared.events.data;

import lombok.Data;

@Data
public class AddressEventData {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
