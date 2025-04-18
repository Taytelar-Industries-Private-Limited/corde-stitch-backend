package com.cordestitch.controller.cache;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.serviceimplementation.cache.CacheServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheServiceImplementation cacheServiceImplementation;
    /**
     * Endpoint to flush the cache and this is only for the testing purpose in dev environment.
     */
    @DeleteMapping("/flushCache")
    public ResponseEntity<SuccessResponse> flushCache(){
        SuccessResponse response = cacheServiceImplementation.flushAllCaches();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}