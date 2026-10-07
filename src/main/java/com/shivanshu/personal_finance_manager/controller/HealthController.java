package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.response.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing health check endpoints for liveness and readiness monitoring.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Public health check endpoint returning application status.
     *
     * @return 200 OK with health status
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {

        return ResponseEntity.ok(new HealthResponse("UP"));
    }
}
