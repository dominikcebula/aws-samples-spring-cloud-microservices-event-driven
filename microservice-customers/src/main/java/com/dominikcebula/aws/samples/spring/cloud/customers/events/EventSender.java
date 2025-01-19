package com.dominikcebula.aws.samples.spring.cloud.customers.events;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSender {
    private final StreamBridge streamBridge;

    public void send(String bindingName, Object data) {
        if (!streamBridge.send(bindingName, data))
            throw new IllegalStateException("Failed to send event [%s] to the stream with data [%s]".formatted(ClassUtils.getName(data), data));
    }
}
