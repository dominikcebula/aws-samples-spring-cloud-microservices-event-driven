package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
abstract class Event {
    private ZonedDateTime timestamp;
}
