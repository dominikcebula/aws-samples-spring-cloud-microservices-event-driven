package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
abstract class Event {
    private ZonedDateTime timestamp;
}
