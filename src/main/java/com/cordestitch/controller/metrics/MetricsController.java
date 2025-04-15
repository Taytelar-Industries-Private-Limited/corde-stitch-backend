package com.cordestitch.controller.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    @GetMapping("/")
    public Map<String, String> getData() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "cordestitch-Backend-Service");
        return response;
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

}
