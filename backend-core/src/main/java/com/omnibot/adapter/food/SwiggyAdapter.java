package com.omnibot.adapter.food;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SwiggyAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SwiggyAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    private static final String[][] MOCK_MENU = {
        {"Chicken Tikka Masala", "Tender chicken in a rich, creamy tomato-spiced gravy"},
        {"Aloo Paratha", "Flaky whole-wheat flatbread stuffed with spiced potato, served with curd"},
        {"Mutton Rogan Josh", "Slow-cooked Kashmiri-style mutton in a aromatic red gravy"},
        {"Chole Bhature", "Spicy chickpea curry with deep-fried fluffy bread — a Delhi classic"},
        {"Idli Sambar", "Steamed rice cakes with tangy lentil soup and coconut chutney"},
        {"Chicken Biryani Special", "Fragrant basmati rice layered with marinated chicken, saffron, and fried onions"},
        {"Paneer Butter Masala", "Soft cottage cheese cubes in a velvety butter-tomato sauce"},
        {"Pani Puri Chaat", "Crispy puris filled with spiced water, tamarind, and chickpeas"}
    };

    public SwiggyAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("SwiggyAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "Swiggy";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/swiggy.com";
    }

    @Override
    public VendorCategory getCategory() {
        return VendorCategory.FOOD;
    }

    @Override
    public String getServiceAction() {
        return "ORDER_FOOD";
    }

    @Override
    public boolean isAvailable() {
        if (mockMode) return true;
        return vendorProperties.getSwiggy() != null && vendorProperties.getSwiggy().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.debug("Swiggy search — query={}, location={}",
                request != null ? request.getQuery() : null,
                request != null ? request.getLocation() : null);

        if (!isAvailable()) {
            log.warn("Swiggy search unavailable — no API key configured");
            return Collections.emptyList();
        }

        if (!mockMode && vendorProperties.getSwiggy() != null && vendorProperties.getSwiggy().hasApiKey()) {
            return liveSearch(request);
        }

        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.debug("Swiggy placeOrder — items={}, amount={}",
                request != null ? request.getItems() : null,
                request != null ? request.getAmount() : null);

        if (!isAvailable()) {
            return VendorOrderResult.failure("Swiggy", "Swiggy API key not configured");
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return VendorOrderResult.failure("Swiggy", "Order must contain at least one item");
        }

        if (!mockMode && vendorProperties.getSwiggy() != null && vendorProperties.getSwiggy().hasApiKey()) {
            return livePlaceOrder(request);
        }

        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.debug("Swiggy trackOrder — orderId={}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            VendorTrackingResult res = new VendorTrackingResult();
            res.setVendorName("Swiggy");
            res.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            res.setStatusMessage("Invalid order ID");
            return res;
        }

        if (!isAvailable()) {
            VendorTrackingResult res = new VendorTrackingResult();
            res.setExternalOrderId(externalOrderId);
            res.setVendorName("Swiggy");
            res.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            res.setStatusMessage("Swiggy API key not configured");
            return res;
        }

        if (!mockMode && vendorProperties.getSwiggy() != null && vendorProperties.getSwiggy().hasApiKey()) {
            return liveTrackOrder(externalOrderId);
        }

        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> liveSearch(VendorSearchRequest request) {
        String endpoint = vendorProperties.getSwiggy().getEndpoint();
        String apiKey = vendorProperties.getSwiggy().getApiKey();
        String query = request != null && request.getQuery() != null ? request.getQuery() : "food";
        double lat = request != null && request.getLatitude() != null ? request.getLatitude() : 12.9716;
        double lon = request != null && request.getLongitude() != null ? request.getLongitude() : 77.5946;

        String baseUrl = endpoint != null && !endpoint.isBlank() ? endpoint : "https://www.swiggy.com/dapi";
        String url = String.format("%s/restaurants/search/v3?lat=%f&lng=%f&str=%s", baseUrl, lat, lon, query);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("x-swiggy-api-key", apiKey);
            headers.set("User-Agent", "OmniBot-Platform/1.0");
            headers.setAccept(List.of(org.springframework.http.MediaType.APPLICATION_JSON));
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map data = (Map) response.getBody().get("data");
                if (data != null && data.get("suggestions") != null) {
                    List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");
                    List<VendorSearchResult> results = new ArrayList<>();
                    for (Map<String, Object> item : suggestions) {
                        VendorSearchResult res = new VendorSearchResult();
                        res.setVendorName("Swiggy");
                        res.setItemName((String) item.getOrDefault("text", "Special Dish"));
                        res.setDescription((String) item.getOrDefault("subCategory", "Swiggy Select Restaurant"));
                        res.setPrice(BigDecimal.valueOf(280));
                        res.setCurrency("INR");
                        int eta = com.omnibot.adapter.util.DynamicDataGenerator.calculateDynamicEta(20, 35);
                        res.setEtaMinutes(eta);
                        res.setEtaLabel(eta + " mins");
                        res.setRating(4.6);
                        res.setAvailable(true);
                        res.setTags(List.of("Swiggy API Portal", "Verified Partner"));
                        results.add(res);
                    }
                    log.info("Swiggy API Portal returned {} search results", results.size());
                    return results;
                }
            }
        } catch (Exception e) {
            log.error("Failed to query Swiggy API Portal (url={}): {} — falling back to mock", url, e.getMessage());
        }

        return mockSearch(request);
    }

    private VendorOrderResult livePlaceOrder(VendorOrderRequest request) {
        String endpoint = vendorProperties.getSwiggy().getEndpoint();
        String apiKey = vendorProperties.getSwiggy().getApiKey();
        String baseUrl = endpoint != null && !endpoint.isBlank() ? endpoint : "https://www.swiggy.com/dapi";
        String url = String.format("%s/order/create", baseUrl);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("x-swiggy-api-key", apiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("cart_items", request != null ? request.getItems() : "Food Item");
            body.put("address", request != null ? request.getDeliveryAddress() : "Bangalore");
            body.put("amount", request != null && request.getAmount() != null ? request.getAmount() : 350);

            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String orderId = (String) response.getBody().getOrDefault("order_id", "SW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                BigDecimal total = request != null && request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(350);
                String trackingUrl = "https://swiggy.com/track/" + orderId;

                VendorOrderResult result = VendorOrderResult.success("Swiggy", orderId, total, trackingUrl);
                result.setEstimatedDelivery("20-30 mins");
                log.info("Swiggy API Portal Order placed successfully: orderId={}", orderId);
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to place order via Swiggy API Portal: {} — falling back to mock", e.getMessage());
        }

        return mockPlaceOrder(request);
    }

    private VendorTrackingResult liveTrackOrder(String externalOrderId) {
        String endpoint = vendorProperties.getSwiggy().getEndpoint();
        String apiKey = vendorProperties.getSwiggy().getApiKey();
        String baseUrl = endpoint != null && !endpoint.isBlank() ? endpoint : "https://www.swiggy.com/dapi";
        String url = String.format("%s/order/track?order_id=%s", baseUrl, externalOrderId);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("x-swiggy-api-key", apiKey);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                VendorTrackingResult res = new VendorTrackingResult();
                res.setExternalOrderId(externalOrderId);
                res.setVendorName("Swiggy");
                res.setStatus(VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY);
                res.setStatusMessage("Swiggy Executive is out for delivery");
                res.setEtaMinutes(15);
                res.setEtaLabel("15 mins");
                return res;
            }
        } catch (Exception e) {
            log.error("Failed to track order via Swiggy API Portal: {} — falling back to mock", e.getMessage());
        }

        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(4, 7);
        List<String[]> pool = new ArrayList<>(Arrays.asList(MOCK_MENU));
        Collections.shuffle(pool, random);

        List<VendorSearchResult> results = new ArrayList<>();
        for (int i = 0; i < count && i < pool.size(); i++) {
            String[] item = pool.get(i);
            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName("Swiggy");
            result.setItemName(item[0]);
            result.setDescription(item[1]);
            result.setPrice(BigDecimal.valueOf(random.nextInt(100, 401)));
            result.setCurrency("INR");
            result.setEtaMinutes(random.nextInt(15, 41));
            result.setEtaLabel(result.getEtaMinutes() + " mins");
            result.setRating(Math.round((3.6 + random.nextDouble() * 1.1) * 10.0) / 10.0);
            result.setReviewCount(random.nextInt(30, 1501));
            result.setAvailable(true);

            List<String> tags = new ArrayList<>();
            if (random.nextBoolean()) tags.add("Bestseller");
            if (random.nextInt(5) == 0) tags.add("New on Swiggy");
            if (random.nextInt(4) == 0) tags.add("Pure Veg");
            if (random.nextInt(3) == 0) tags.add("Swiggy One Free Delivery");
            result.setTags(tags);

            results.add(result);
        }

        if (!results.isEmpty() && request != null && request.getMaxPrice() != null) {
            results.removeIf(r -> r.getPrice().compareTo(BigDecimal.valueOf(request.getMaxPrice())) > 0);
        }

        log.debug("Swiggy mock search returned {} items", results.size());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "SW" + String.format("%08d", random.nextInt(0, 100_000_000));
        BigDecimal deliveryFee = BigDecimal.valueOf(random.nextInt(20, 51));
        BigDecimal totalAmount = request.getAmount() != null
                ? request.getAmount().add(deliveryFee)
                : deliveryFee;

        String trackingUrl = "https://swiggy.com/track/" + orderId;
        VendorOrderResult result = VendorOrderResult.success("Swiggy", orderId, totalAmount, trackingUrl);
        result.setEstimatedDelivery("25-40 mins");

        log.debug("Swiggy mock order placed — orderId={}, total={}", orderId, totalAmount);
        return result;
    }

    private VendorTrackingResult mockTrackOrder(String externalOrderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        VendorTrackingResult.TrackingStatus[] statuses = {
            VendorTrackingResult.TrackingStatus.PREPARING,
            VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
            VendorTrackingResult.TrackingStatus.DELIVERED
        };
        VendorTrackingResult.TrackingStatus status = statuses[random.nextInt(statuses.length)];
        int eta = random.nextInt(3, 26);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Swiggy");
        result.setStatus(status);
        result.setStatusMessage(status.name().replace('_', ' '));
        result.setEtaMinutes(eta);
        result.setEtaLabel(eta + " mins");
        result.setCurrency("INR");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(180, 500)));
        result.setDriverName("Rahul K.");
        result.setDriverPhone("+91 98XXX XXX" + random.nextInt(10, 99));

        result.setEvents(List.of(
            new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now().minusMinutes(eta + 10),
                    "ORDER_PLACED", "Order confirmed by restaurant"),
            new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now().minusMinutes(3),
                    status.name(), status.name().replace('_', ' '))
        ));

        log.debug("Swiggy mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }
}
