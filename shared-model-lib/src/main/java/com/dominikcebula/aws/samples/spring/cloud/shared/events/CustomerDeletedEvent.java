package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import com.dominikcebula.aws.samples.spring.cloud.shared.events.data.CustomerEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeletedEvent extends CustomerEvent {
    private CustomerEventData customerEventData;
}
