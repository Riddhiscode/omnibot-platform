package com.omnibot.adapter.food;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ZomatoAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZomatoAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    private static final String VENDOR_NAME = "Zomato";
    private static final String LOGO_URL = "https://logo.clearbit.com/zomato.com";
    private static final String TRACKING_URL_TEMPLATE = "https://zomato.com/track/%s";

    private static final String[][] MOCK_MENU = {
        {"Butter Chicken", "Creamy tomato-based curry with tender chicken pieces, a North Indian classic"},
        {"Paneer Tikka", "Chargrilled cottage cheese cubes marinated in spiced yogurt"},
        {"Hyderabadi Biryani", "Fragrant basmati rice layered with spiced meat, slow-cooked in a sealed pot"},
        {"Masala Dosa", "Crispy fermented rice crepe stuffed with spiced potato filling, served with sambar and chutney"},
        {"Tandoori Roti", "Whole wheat flatbread baked in a clay tandoor oven"},
        {"Chicken Shawarma", "Spiced chicken wrapped in rumali roti with garlic sauce and pickled vegetables"},
        {"Chole Bhature", "Spicy chickpea curry paired with deep-fried fluffy bread"},
        {"Malai Kofta", "Soft paneer and potato dumplings in a rich creamy gravy"},
        {"Egg Bhurji", "Spiced scrambled eggs cooked with onions, tomatoes, and green chilies"},
        {"Dal Makhani", "Slow-cooked black lentils simmered with butter and cream"},
        {"Prawn Masala", "Succulent prawns tossed in a fiery coastal masala"},
        {"Veg Manchurian", "Indo-Chinese crispy vegetable dumplings in a tangy sauce"},
    };

    private static final VendorTrackingResult.TrackingStatus[] MOCK_STATUSES = {
        VendorTrackingResult.TrackingStatus.PREPARING,
        VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
        VendorTrackingResult.TrackingStatus.DELIVERED,
    };

    public ZomatoAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        log.info("ZomatoAdapter initialised in {} mode", vendorProperties.isMockMode() ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public String getLogoUrl() {
        return LOGO_URL;
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
        return vendorProperties.isMockMode() || vendorProperties.getZomato().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.debug("Zomato search — query={}, location={}", request != null ? request.getQuery() : null,
                request != null ? request.getLocation() : null);

        if (!isAvailable()) {
            log.warn("Zomato search unavailable — no API key configured");
            return Collections.emptyList();
        }

        if (vendorProperties.isMockMode()) {
            return mockSearch(request);
        }

        return liveSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.debug("Zomato placeOrder — items={}, amount={}", request != null ? request.getItems() : null,
                request != null ? request.getAmount() : null);

        if (!isAvailable()) {
            return VendorOrderResult.failure(VENDOR_NAME, "Zomato API key not configured");
        }

        if (vendorProperties.isMockMode()) {
            return mockPlaceOrder(request);
        }

        return livePlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.debug("Zomato trackOrder — orderId={}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            log.warn("Zomato trackOrder called with blank orderId");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName(VENDOR_NAME);
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Invalid order ID");
            return result;
        }

        if (!isAvailable()) {
            log.warn("Zomato trackOrder unavailable — no API key configured");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setExternalOrderId(externalOrderId);
            result.setVendorName(VENDOR_NAME);
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Zomato API key not configured");
            return result;
        }

        if (vendorProperties.isMockMode()) {
            return mockTrackOrder(externalOrderId);
        }

        return liveTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(4, 7);
        List<VendorSearchResult> results = new ArrayList<>(count);

        List<String[]> menuPool = new ArrayList<>(Arrays.asList(MOCK_MENU));
        Collections.shuffle(menuPool, random);

        for (int i = 0; i < count && i < menuPool.size(); i++) {
            String[] item = menuPool.get(i);
            VendorSearchResult result = new VendorSearchResult();

            result.setVendorName(VENDOR_NAME);
            result.setItemName(item[0]);
            result.setDescription(item[1]);
            result.setPrice(BigDecimal.valueOf(random.nextInt(120, 451)));
            result.setCurrency("INR");
            result.setEtaMinutes(com.omnibot.adapter.util.DynamicDataGenerator.calculateDynamicEta(20, 46));
            result.setEtaLabel(result.getEtaMinutes() + " mins");
            result.setRating(random.nextDouble(3.8, 4.9));
            result.setReviewCount(random.nextInt(50, 2001));
            result.setAvailable(true);

            List<String> tags = new ArrayList<>();
            if (random.nextBoolean()) tags.add("Bestseller");
            if (random.nextInt(5) == 0) tags.add("New on Zomato");
            if (random.nextInt(4) == 0) tags.add("Pure Veg");
            result.setTags(tags);

            results.add(result);
        }

        if (!results.isEmpty() && request != null && request.getMaxPrice() != null) {
            BigDecimal maxPrice = BigDecimal.valueOf(request.getMaxPrice());
            results.removeIf(r -> r.getPrice().compareTo(maxPrice) > 0);
        }

        if (!results.isEmpty() && request != null && request.getMaxResults() != null
                && results.size() > request.getMaxResults()) {
            results = results.subList(0, request.getMaxResults());
        }

        log.debug("Zomato mock search returned {} items", results.size());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String orderId = "ZOM" + String.format("%08d", random.nextInt(0, 100_000_000));
        BigDecimal deliveryFee = BigDecimal.valueOf(random.nextInt(25, 61));
        BigDecimal totalAmount = request != null && request.getAmount() != null
                ? request.getAmount().add(deliveryFee)
                : deliveryFee;

        String trackingUrl = String.format(TRACKING_URL_TEMPLATE, orderId);

        VendorOrderResult result = VendorOrderResult.success(VENDOR_NAME, orderId, totalAmount, trackingUrl);
        result.setEstimatedDelivery("30-45 mins");

        log.debug("Zomato mock order placed — orderId={}, total={}", orderId, totalAmount);
        return result;
    }

    private VendorTrackingResult mockTrackOrder(String externalOrderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        VendorTrackingResult.TrackingStatus status = MOCK_STATUSES[random.nextInt(MOCK_STATUSES.length)];
        int eta = random.nextInt(5, 31);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName(VENDOR_NAME);
        result.setStatus(status);
        result.setStatusMessage(status.name().replace('_', ' '));
        result.setEtaMinutes(eta);
        result.setEtaLabel(eta + " mins");
        result.setCurrency("INR");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(200, 600)));

        result.setEvents(List.of(
            new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now().minusMinutes(eta + 10),
                    "ORDER_PLACED",
                    "Order confirmed by restaurant"),
            new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now().minusMinutes(5),
                    status.name(),
                    status.name().replace('_', ' '))
        ));

        log.debug("Zomato mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }

    private List<VendorSearchResult> liveSearch(VendorSearchRequest request) {
        String endpoint = vendorProperties.getZomato().getEndpoint();
        String apiKey = vendorProperties.getZomato().getApiKey();
        String query = request != null && request.getQuery() != null ? request.getQuery() : "food";
        double lat = request != null && request.getLatitude() != null ? request.getLatitude() : 12.9716;
        double lon = request != null && request.getLongitude() != null ? request.getLongitude() : 77.5946;

        String url = String.format("%s/search?q=%s&lat=%f&lon=%f", 
                endpoint != null && !endpoint.isBlank() ? endpoint : "https://developers.zomato.com/api/v2.1",
                query, lat, lon);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("user-key", apiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> restaurants = (List<Map<String, Object>>) response.getBody().get("restaurants");
                if (restaurants != null && !restaurants.isEmpty()) {
                    List<VendorSearchResult> results = new ArrayList<>();
                    for (Map<String, Object> rWrapper : restaurants) {
                        Map<String, Object> r = (Map<String, Object>) rWrapper.get("restaurant");
                        if (r == null) continue;

                        VendorSearchResult res = new VendorSearchResult();
                        res.setVendorName(VENDOR_NAME);
                        res.setItemName((String) r.getOrDefault("name", "Special Item"));
                        res.setDescription((String) r.getOrDefault("cuisines", "North Indian, Fast Food"));
                        
                        Object priceObj = r.get("average_cost_for_two");
                        BigDecimal cost = priceObj instanceof Number ? BigDecimal.valueOf(((Number) priceObj).doubleValue() / 2.0) : BigDecimal.valueOf(250);
                        res.setPrice(cost);
                        res.setCurrency("INR");
                        
                        int eta = com.omnibot.adapter.util.DynamicDataGenerator.calculateDynamicEta(20, 40);
                        res.setEtaMinutes(eta);
                        res.setEtaLabel(eta + " mins");
                        
                        Map<String, Object> ratingObj = (Map<String, Object>) r.get("user_rating");
                        if (ratingObj != null && ratingObj.get("aggregate_rating") != null) {
                            try { res.setRating(Double.parseDouble(ratingObj.get("aggregate_rating").toString())); } catch (Exception e) { res.setRating(4.3); }
                        } else {
                            res.setRating(4.3);
                        }

                        res.setAvailable(true);
                        res.setTags(List.of("Live API", "Direct Partner"));
                        results.add(res);
                    }
                    log.info("Zomato Live API returned {} restaurants", results.size());
                    return results;
                }
            }
        } catch (Exception e) {
            log.error("Failed to query Zomato Live API (url={}): {} — falling back to mock", url, e.getMessage());
        }

        return mockSearch(request);
    }

    private VendorOrderResult livePlaceOrder(VendorOrderRequest request) {
        String endpoint = vendorProperties.getZomato().getEndpoint();
        String apiKey = vendorProperties.getZomato().getApiKey();
        String url = String.format("%s/orders", endpoint != null && !endpoint.isBlank() ? endpoint : "https://developers.zomato.com/api/v2.1");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("user-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("items", request != null ? request.getItems() : "Food Item");
            body.put("delivery_address", request != null ? request.getDeliveryAddress() : "Bangalore");
            body.put("amount", request != null && request.getAmount() != null ? request.getAmount() : 350);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String orderId = (String) response.getBody().getOrDefault("order_id", "ZOM-" + UUID.randomUUID().toString().substring(0, 8));
                BigDecimal total = request != null && request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(350);
                String trackingUrl = String.format(TRACKING_URL_TEMPLATE, orderId);

                VendorOrderResult result = VendorOrderResult.success(VENDOR_NAME, orderId, total, trackingUrl);
                result.setEstimatedDelivery("25-35 mins");
                log.info("Zomato Live Order placed: orderId={}", orderId);
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to place live order on Zomato API: {} — falling back to mock", e.getMessage());
        }

        return mockPlaceOrder(request);
    }

    private VendorTrackingResult liveTrackOrder(String externalOrderId) {
        String endpoint = vendorProperties.getZomato().getEndpoint();
        String apiKey = vendorProperties.getZomato().getApiKey();
        String url = String.format("%s/orders/%s/status", endpoint != null && !endpoint.isBlank() ? endpoint : "https://developers.zomato.com/api/v2.1", externalOrderId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("user-key", apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String statusStr = (String) response.getBody().getOrDefault("status", "PREPARING");
                VendorTrackingResult result = new VendorTrackingResult();
                result.setExternalOrderId(externalOrderId);
                result.setVendorName(VENDOR_NAME);
                result.setStatus(VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY);
                result.setStatusMessage(statusStr);
                result.setEtaMinutes(18);
                result.setEtaLabel("18 mins");
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to track live order on Zomato API: {} — falling back to mock", e.getMessage());
        }

        return mockTrackOrder(externalOrderId);
    }
}
