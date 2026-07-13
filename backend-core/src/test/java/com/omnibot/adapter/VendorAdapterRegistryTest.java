package com.omnibot.adapter;

import com.omnibot.adapter.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class VendorAdapterRegistryTest {

    private VendorAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new VendorAdapterRegistry();
    }

    @Test
    void registerAdapterAndLookupByName() {
        VendorAdapter adapter = createMockAdapter("Zomato", VendorCategory.FOOD);
        registry.register(adapter);

        Optional<VendorAdapter> found = registry.getAdapter("Zomato");
        assertTrue(found.isPresent());
        assertEquals("Zomato", found.get().getVendorName());
    }

    @Test
    void registerAdapterAndLookupByNameAndCategory() {
        VendorAdapter adapter = createMockAdapter("Zomato", VendorCategory.FOOD);
        registry.register(adapter);

        Optional<VendorAdapter> found = registry.getAdapter("Zomato", VendorCategory.FOOD);
        assertTrue(found.isPresent());

        Optional<VendorAdapter> notFound = registry.getAdapter("Zomato", VendorCategory.TRANSPORT);
        assertFalse(notFound.isPresent());
    }

    @Test
    void getAdaptersByCategory() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));
        registry.register(createMockAdapter("Swiggy", VendorCategory.FOOD));
        registry.register(createMockAdapter("Uber", VendorCategory.TRANSPORT));

        List<VendorAdapter> foodAdapters = registry.getAdapters(VendorCategory.FOOD);
        assertEquals(2, foodAdapters.size());

        List<VendorAdapter> transportAdapters = registry.getAdapters(VendorCategory.TRANSPORT);
        assertEquals(1, transportAdapters.size());

        List<VendorAdapter> shoppingAdapters = registry.getAdapters(VendorCategory.SHOPPING);
        assertTrue(shoppingAdapters.isEmpty());
    }

    @Test
    void getAvailableAdaptersFiltersUnavailable() {
        VendorAdapter available = createMockAdapter("Zomato", VendorCategory.FOOD);
        VendorAdapter unavailable = new VendorAdapter() {
            @Override public String getVendorName() { return "Swiggy"; }
            @Override public String getLogoUrl() { return "https://logo.test/swiggy"; }
            @Override public VendorCategory getCategory() { return VendorCategory.FOOD; }
            @Override public String getServiceAction() { return "MOCK"; }
            @Override public boolean isAvailable() { return false; }
            @Override public java.util.List<VendorSearchResult> search(VendorSearchRequest r) { return java.util.List.of(); }
            @Override public VendorOrderResult placeOrder(VendorOrderRequest r) { return VendorOrderResult.failure("Swiggy", "unavailable"); }
            @Override public VendorTrackingResult trackOrder(String id) { return new VendorTrackingResult(); }
        };

        registry.register(available);
        registry.register(unavailable);

        List<VendorAdapter> availableAdapters = registry.getAvailableAdapters(VendorCategory.FOOD);
        assertEquals(1, availableAdapters.size());
        assertEquals("Zomato", availableAdapters.get(0).getVendorName());
    }

    @Test
    void getAllAdaptersReturnsAll() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));
        registry.register(createMockAdapter("Uber", VendorCategory.TRANSPORT));
        registry.register(createMockAdapter("Amazon", VendorCategory.SHOPPING));

        List<VendorAdapter> all = registry.getAllAdapters();
        assertEquals(3, all.size());
    }

    @Test
    void searchAllAggregatesResultsAndMarksCheapestFastest() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));
        registry.register(createMockAdapter("Swiggy", VendorCategory.FOOD));

        VendorSearchRequest request = new VendorSearchRequest();
        request.setQuery("biryani");
        request.setLatitude(12.9716);
        request.setLongitude(77.5946);

        List<VendorSearchResult> results = registry.searchAll(VendorCategory.FOOD, request);
        assertFalse(results.isEmpty());

        long cheapestCount = results.stream().filter(VendorSearchResult::isCheapest).count();
        long fastestCount = results.stream().filter(VendorSearchResult::isFastest).count();
        assertEquals(1, cheapestCount);
        assertEquals(1, fastestCount);
    }

    @Test
    void placeOrderWithUnknownVendorReturnsFailure() {
        VendorOrderRequest request = new VendorOrderRequest();
        request.setVendorName("UnknownVendor");
        request.setUserId(1L);

        VendorOrderResult result = registry.placeOrder("UnknownVendor", VendorCategory.FOOD, request);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Vendor not found"));
    }

    @Test
    void placeOrderDelegatesToCorrectAdapter() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));

        VendorOrderRequest request = new VendorOrderRequest();
        request.setVendorName("Zomato");
        request.setUserId(1L);

        VendorOrderResult result = registry.placeOrder("Zomato", VendorCategory.FOOD, request);
        assertTrue(result.isSuccess());
        assertEquals("Zomato", result.getVendorName());
    }

    @Test
    void trackOrderWithUnknownVendorReturnsFailure() {
        VendorTrackingResult result = registry.trackOrder("UnknownVendor", "ORD-123");
        assertEquals(VendorTrackingResult.TrackingStatus.FAILED, result.getStatus());
        assertTrue(result.getStatusMessage().contains("Vendor not found"));
    }

    @Test
    void getVendorStatusReturnsAllCategories() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));
        registry.register(createMockAdapter("Uber", VendorCategory.TRANSPORT));
        registry.register(createMockAdapter("Amazon", VendorCategory.SHOPPING));

        Map<String, Object> status = registry.getVendorStatus();
        assertTrue(status.containsKey("FOOD"));
        assertTrue(status.containsKey("TRANSPORT"));
        assertTrue(status.containsKey("SHOPPING"));
    }

    @Test
    void caseInsensitiveLookup() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));

        assertTrue(registry.getAdapter("zomato").isPresent());
        assertTrue(registry.getAdapter("ZOMATO").isPresent());
        assertTrue(registry.getAdapter("Zomato").isPresent());
    }

    @Test
    void duplicateRegistrationReplaces() {
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));
        registry.register(createMockAdapter("Zomato", VendorCategory.FOOD));

        List<VendorAdapter> foodAdapters = registry.getAdapters(VendorCategory.FOOD);
        assertEquals(1, foodAdapters.size());
    }

    // --- Helper: create a simple mock adapter for testing the registry ---

    private VendorAdapter createMockAdapter(String name, VendorCategory category) {
        return new VendorAdapter() {
            @Override public String getVendorName() { return name; }
            @Override public String getLogoUrl() { return "https://logo.test/" + name.toLowerCase(); }
            @Override public VendorCategory getCategory() { return category; }
            @Override public String getServiceAction() { return "MOCK_ACTION"; }
            @Override public boolean isAvailable() { return true; }
            @Override public List<VendorSearchResult> search(VendorSearchRequest req) {
                VendorSearchResult r = new VendorSearchResult();
                r.setVendorName(name);
                r.setItemName("Mock Item from " + name);
                r.setPrice(BigDecimal.valueOf(name.length() * 10));
                r.setEtaMinutes(name.length() * 5);
                r.setCurrency("INR");
                r.setAvailable(true);
                return List.of(r);
            }
            @Override public VendorOrderResult placeOrder(VendorOrderRequest req) {
                return VendorOrderResult.success(name, "ORD-" + System.currentTimeMillis(),
                        BigDecimal.valueOf(299), "http://track.test/123");
            }
            @Override public VendorTrackingResult trackOrder(String orderId) {
                VendorTrackingResult t = new VendorTrackingResult();
                t.setExternalOrderId(orderId);
                t.setVendorName(name);
                t.setStatus(VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY);
                t.setStatusMessage("On the way");
                return t;
            }
        };
    }
}
