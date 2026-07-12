package com.omnibot.controller;

import com.omnibot.agent.MockServiceAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/aggregator")
public class AggregatorController {

    private final MockServiceAdapter mockServiceAdapter;

    public AggregatorController(MockServiceAdapter mockServiceAdapter) {
        this.mockServiceAdapter = mockServiceAdapter;
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> comparePrices(@RequestBody Map<String, Object> request) {
        String intent = request.get("intent") != null ? request.get("intent").toString() : "UNKNOWN";

        List<Map<String, Object>> options = mockServiceAdapter.getComparisonOptions(intent);

        Map<String, Object> response = new HashMap<>();
        response.put("options", options);
        response.put("cheapest_vendor", mockServiceAdapter.findCheapestVendor(options));
        response.put("fastest_vendor", mockServiceAdapter.findFastestVendor(options));
        response.put("vendor_count", options.size());

        return ResponseEntity.ok(response);
    }
}
