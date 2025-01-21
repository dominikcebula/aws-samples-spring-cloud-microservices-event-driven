package com.dominikcebula.aws.samples.spring.cloud.shared.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(
        use = NAME,
        include = PROPERTY,
        property = "eventType")
@JsonSubTypes({
        @Type(value = CustomerCreatedEvent.class, name = "created"),
        @Type(value = CustomerUpdatedEvent.class, name = "updated"),
        @Type(value = CustomerDeletedEvent.class, name = "deleted"),
})
public abstract class CustomerEvent extends Event {
}
