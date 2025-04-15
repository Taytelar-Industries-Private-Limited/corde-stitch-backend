package com.cordestitch.service.serviceimplementation.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    private final Counter customCounter;

    public SampleService(MeterRegistry registry) {
        this.customCounter = registry.counter("custom_counter");
    }

    public void performAction() {
        customCounter.increment();
        // Your business logic here
    }
}