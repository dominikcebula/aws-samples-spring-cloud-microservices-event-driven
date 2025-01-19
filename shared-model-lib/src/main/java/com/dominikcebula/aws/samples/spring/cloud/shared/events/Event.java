package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
abstract class Event {
    private ZonedDateTime timestamp;

    @SuppressWarnings("unused")
    public Event() {
        this.timestamp = ZonedDateTime.now();
    }
}
