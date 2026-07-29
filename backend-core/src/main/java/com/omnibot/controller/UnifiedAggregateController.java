package com.omnibot.controller;

import com.omnibot.adapter.VendorAdapterRegistry;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.AggregatedItem;
import com.omnibot.adapter.dto.VendorSearchRequest;
import com.omnibot.adapter.dto.VendorSearchResult;
import com.omnibot.adapter.util.DeepLinkGenerator;
import com.omnibot.adapter.util.DynamicDataGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/aggregate", "/v1/aggregator/aggregate", "/v1/aggregate"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class UnifiedAggregateController {

    private final VendorAdapterRegistry registry;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public UnifiedAggregateController(VendorAdapterRegistry registry) {
        this.registry = registry;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> aggregate(@RequestBody Map<String, Object> body) {
        String catStr = body.get("serviceCategory") != null 
            ? body.get("serviceCategory").toString() 
            : (body.get("category") != null ? body.get("category").toString() : "FOOD");
        
        String query = body.get("query") != null ? body.get("query").toString() : "burger";
        double lat = body.get("latitude") != null ? Double.parseDouble(body.get("latitude").toString()) : 12.97;
        double lon = body.get("longitude") != null ? Double.parseDouble(body.get("longitude").toString()) : 77.59;

        VendorCategory category;
        try {
            category = VendorCategory.valueOf(catStr.toUpperCase());
        } catch (Exception e) {
            category = VendorCategory.FOOD;
        }

        VendorSearchRequest searchReq = new VendorSearchRequest(query, "Bangalore");
        searchReq.setLatitude(lat);
        searchReq.setLongitude(lon);

        final VendorCategory finalCategory = category;
        final VendorSearchRequest finalReq = searchReq;

        // Circuit breaker: 800ms timeout for direct API layer
        List<VendorSearchResult> liveResults;
        try {
            CompletableFuture<List<VendorSearchResult>> future = CompletableFuture.supplyAsync(
                () -> registry.searchAll(finalCategory, finalReq), executorService
            );
            liveResults = future.get(800, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            liveResults = Collections.emptyList(); // Circuit breaker tripped -> Layer B Fallback
        }

        List<AggregatedItem> items = new ArrayList<>();

        // Layer A: Populate from live adapter search results
        for (VendorSearchResult res : liveResults) {
            String actionType = DeepLinkGenerator.resolveActionType(res.getVendorName());
            String deepLink = DeepLinkGenerator.generateDeepLink(res.getVendorName(), category.name(), query, lat, lon);
            String etaLabel = res.getEtaMinutes() > 0 ? res.getEtaMinutes() + " mins" : "15 mins";
            String priceLabel = res.getPrice() != null ? "₹" + res.getPrice().intValue() : "₹150";

            AggregatedItem item = new AggregatedItem(
                res.getVendorName(),
                category.name(),
                etaLabel,
                priceLabel,
                4.2,
                deepLink,
                actionType,
                "⚡"
            );
            item.setCheapest(res.isCheapest());
            item.setFastest(res.isFastest());
            items.add(item);
        }

        // Layer B: Ensure non-adapter quick commerce / proprietary platforms (Rapido, Flipkart Minutes, Nykaa, Zepto) are included
        List<String> targetPlatforms = getTargetPlatformsForCategory(category);
        for (String platform : targetPlatforms) {
            boolean alreadyPresent = items.stream().anyMatch(i -> i.getProvider().equalsIgnoreCase(platform));
            if (!alreadyPresent) {
                String deepLink = DeepLinkGenerator.generateDeepLink(platform, category.name(), query, lat, lon);
                int dynamicEta = DynamicDataGenerator.calculateDynamicEta(15, 25);
                
                AggregatedItem item = new AggregatedItem(
                    platform,
                    category.name(),
                    dynamicEta + " mins",
                    category == VendorCategory.TRANSPORT ? "₹45 - ₹120" : "Free Delivery",
                    4.3,
                    deepLink,
                    "DEEP_LINK",
                    "↗"
                );
                items.add(item);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category.name());
        response.put("query", query);
        response.put("itemCount", items.size());
        response.put("items", items);

        return ResponseEntity.ok(response);
    }

    private List<String> getTargetPlatformsForCategory(VendorCategory category) {
        switch (category) {
            case FOOD:
                return List.of("Zomato", "Swiggy", "Blinkit", "Zepto");
            case TRANSPORT:
                return List.of("Uber", "Ola", "Rapido", "Yulu");
            case SHOPPING:
                return List.of("Amazon", "Flipkart", "Nykaa", "Meesho", "Myntra");
            case GROCERY:
                return List.of("BigBasket", "JioMart", "Blinkit", "Zepto");
            default:
                return List.of("Zomato", "Uber", "Amazon");
        }
    }
}
