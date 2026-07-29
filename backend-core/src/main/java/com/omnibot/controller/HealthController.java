package com.omnibot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class HealthController {

    @RequestMapping(
        value = {"/health", "/api/health", "/api/v1/health", "/v1/health"},
        method = {RequestMethod.GET, RequestMethod.HEAD}
    )
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "OmniBot is running"));
    }
}
