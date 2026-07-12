package com.omnibot.adapter;

import com.omnibot.adapter.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry for all vendor adapters.
 * Adapters self-register on startup via VendorAdapterConfig.
 *
 * Lookup methods:
 *   getAdapter(name, category)  — single vendor
 *   getAdapters(category)       — all vendors in a category
 *   getAllAdapters()             — every registered vendor
 *   searchAll(category, req)    — parallel search across all vendors in a category
 */
public class VendorAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(VendorAdapterRegistry.class);

    private final Map<String, VendorAdapter> adaptersByName = new ConcurrentHashMap<>();
    private final Map<VendorCategory, List<VendorAdapter>> adaptersByCategory = new ConcurrentHashMap<>();

    public void register(VendorAdapter adapter) {
        String key = adapterKey(adapter.getVendorName(), adapter.getCategory());
        adaptersByName.put(key.toLowerCase(), adapter);
        adaptersByCategory.computeIfAbsent(adapter.getCategory(), k -> new ArrayList<>()).add(adapter);
        log.info("Registered vendor adapter: {} [{}] — available={}",
                adapter.getVendorName(), adapter.getCategory(), adapter.isAvailable());
    }

    public Optional<VendorAdapter> getAdapter(String vendorName, VendorCategory category) {
        String key = vendorName.toLowerCase() + ":" + category.name();
        return Optional.ofNullable(adaptersByName.get(key));
    }

    public Optional<VendorAdapter> getAdapter(String vendorName) {
        return adaptersByName.values().stream()
                .filter(a -> a.getVendorName().equalsIgnoreCase(vendorName))
                .findFirst();
    }

    public List<VendorAdapter> getAdapters(VendorCategory category) {
        return adaptersByCategory.getOrDefault(category, Collections.emptyList());
    }

    public List<VendorAdapter> getAvailableAdapters(VendorCategory category) {
        return getAdapters(category).stream()
                .filter(VendorAdapter::isAvailable)
                .collect(Collectors.toList());
    }

    public List<VendorAdapter> getAllAdapters() {
        return new ArrayList<>(adaptersByName.values());
    }

    public List<VendorAdapter> getAllAvailableAdapters() {
        return adaptersByName.values().stream()
                .filter(VendorAdapter::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Search all available vendors in a category and return combined results,
     * with cheapest/fastest flags set across the full result set.
     */
    public List<VendorSearchResult> searchAll(VendorCategory category, VendorSearchRequest request) {
        List<VendorAdapter> available = getAvailableAdapters(category);
        List<VendorSearchResult> allResults = new ArrayList<>();

        for (VendorAdapter adapter : available) {
            try {
                List<VendorSearchResult> results = adapter.search(request);
                allResults.addAll(results);
            } catch (Exception e) {
                log.error("Error searching {} — {}", adapter.getVendorName(), e.getMessage());
            }
        }

        markCheapestAndFastest(allResults);
        return allResults;
    }

    /**
     * Place an order with a specific vendor by name.
     */
    public VendorOrderResult placeOrder(String vendorName, VendorCategory category, VendorOrderRequest request) {
        Optional<VendorAdapter> adapterOpt = getAdapter(vendorName, category);
        if (adapterOpt.isEmpty()) {
            return VendorOrderResult.failure(vendorName, "Vendor not found: " + vendorName);
        }

        VendorAdapter adapter = adapterOpt.get();
        if (!adapter.isAvailable()) {
            return VendorOrderResult.failure(vendorName, "Vendor is currently unavailable: " + vendorName);
        }

        return adapter.placeOrder(request);
    }

    /**
     * Track an order — looks up the right adapter by vendor name.
     */
    public VendorTrackingResult trackOrder(String vendorName, String externalOrderId) {
        Optional<VendorAdapter> adapterOpt = getAdapter(vendorName);
        if (adapterOpt.isEmpty()) {
            VendorTrackingResult result = new VendorTrackingResult();
            result.setExternalOrderId(externalOrderId);
            result.setVendorName(vendorName);
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Vendor not found: " + vendorName);
            return result;
        }

        return adapterOpt.get().trackOrder(externalOrderId);
    }

    /**
     * Get a summary of all registered vendors for diagnostic/health endpoints.
     */
    public Map<String, Object> getVendorStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        for (VendorCategory cat : VendorCategory.values()) {
            List<Map<String, Object>> vendorList = getAdapters(cat).stream()
                    .map(a -> {
                        Map<String, Object> v = new LinkedHashMap<>();
                        v.put("name", a.getVendorName());
                        v.put("available", a.isAvailable());
                        v.put("action", a.getServiceAction());
                        return v;
                    })
                    .collect(Collectors.toList());
            status.put(cat.name(), vendorList);
        }
        status.put("mode", adaptersByName.values().stream().findFirst()
                .map(a -> a.getClass().getSimpleName().contains("Mock") ? "mock" : "detected")
                .orElse("unknown"));
        return status;
    }

    private void markCheapestAndFastest(List<VendorSearchResult> results) {
        if (results.isEmpty()) return;

        Optional<VendorSearchResult> cheapest = results.stream()
                .min(Comparator.comparing(VendorSearchResult::getPrice));
        cheapest.ifPresent(c -> c.setCheapest(true));

        Optional<VendorSearchResult> fastest = results.stream()
                .min(Comparator.comparingInt(VendorSearchResult::getEtaMinutes));
        fastest.ifPresent(f -> f.setFastest(true));
    }

    private String adapterKey(String vendorName, VendorCategory category) {
        return vendorName.toLowerCase() + ":" + category.name();
    }
}
