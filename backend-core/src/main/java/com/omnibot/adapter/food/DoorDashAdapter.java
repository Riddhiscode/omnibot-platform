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
public class DoorDashAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoorDashAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    public DoorDashAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("DoorDashAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "DoorDash";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/doordash.com";
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
        if (mockMode) {
            return true;
        }
        return vendorProperties.getDoordash() != null
                && vendorProperties.getDoordash().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.info("DoorDash search request: {}", request);

        if (!isAvailable()) {
            log.warn("DoorDash is not available — cannot perform search");
            return Collections.emptyList();
        }

        if (!mockMode) {
            // TODO: Implement real DoorDash Marketplace API call
            // 1. Build DoorDash search request with query, location, filters
            // 2. Send HTTP GET to /store/v2/search or /marketplace/catalog endpoint
            // 3. Parse response into VendorSearchResult list
            // 4. Handle pagination via cursor-based tokens
            log.warn("Live DoorDash API not yet configured — falling back to mock");
        }

        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.info("DoorDash placeOrder request: {}", request);

        if (!isAvailable()) {
            log.warn("DoorDash is not available — cannot place order");
            return VendorOrderResult.failure("DoorDash", "DoorDash is not available");
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("Cannot place DoorDash order with null or empty items");
            return VendorOrderResult.failure("DoorDash", "Order must contain at least one item");
        }

        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            log.warn("Cannot place DoorDash order without a delivery address");
            return VendorOrderResult.failure("DoorDash", "Delivery address is required");
        }

        if (!mockMode) {
            // TODO: Implement real DoorDash order placement
            // 1. Validate items via DoorDash catalog API
            // 2. Calculate final price including taxes and delivery fee
            // 3. POST to DoorDash order creation endpoint
            // 4. Return confirmation with external order ID
            log.warn("Live DoorDash API not yet configured — falling back to mock");
        }

        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.info("DoorDash trackOrder request for orderId: {}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            log.warn("Cannot track DoorDash order with null or blank orderId");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName("DoorDash");
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Order ID is required");
            return result;
        }

        if (!isAvailable()) {
            log.warn("DoorDash is not available — cannot track order");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName("DoorDash");
            result.setExternalOrderId(externalOrderId);
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("DoorDash is not available");
            return result;
        }

        if (!mockMode) {
            // TODO: Implement real DoorDash order tracking
            // 1. GET from DoorDash order status API using externalOrderId
            // 2. Parse delivery partner details, ETA, GPS location
            // 3. Map DoorDash status enums to VendorTrackingResult.TrackingStatus
            log.warn("Live DoorDash API not yet configured — falling back to mock");
        }

        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String[][] menuPool = {
                {"BBQ Pulled Pork Burger", "Smoky pulled pork with tangy BBQ sauce on a brioche bun"},
                {"Nashville Hot Chicken Sandwich", "Crispy fried chicken with cayenne-spiced oil and pickles"},
                {"Loaded Nachos Supreme", "Tortilla chips piled with cheese, jalapeños, sour cream, and guac"},
                {"Thai Green Curry", "Fragrant coconut curry with Thai basil, bamboo shoots, and jasmine rice"},
                {"Vegan Buddha Bowl", "Quinoa, roasted sweet potato, avocado, chickpeas, tahini drizzle"},
                {"Double Smash Burger", "Two seared patties, American cheese, caramelized onions, special sauce"}
        };

        int itemCount = random.nextInt(4, 7);
        List<VendorSearchResult> results = new ArrayList<>();

        List<String[]> pool = new ArrayList<>(Arrays.asList(menuPool));
        Collections.shuffle(pool, random);

        for (int i = 0; i < Math.min(itemCount, pool.size()); i++) {
            String[] entry = pool.get(i);
            String name = entry[0];
            String description = entry[1];
            BigDecimal price = BigDecimal.valueOf(random.nextInt(180, 551));
            int deliveryMinutes = random.nextInt(30, 56);
            double rating = Math.round((4.0 + random.nextDouble() * 0.8) * 10.0) / 10.0;

            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName("DoorDash");
            result.setItemName(name);
            result.setDescription(description);
            result.setPrice(price);
            result.setCurrency("INR");
            result.setEtaMinutes(deliveryMinutes);
            result.setEtaLabel(deliveryMinutes + " min");
            result.setRating(rating);
            result.setReviewCount(random.nextInt(50, 800));
            result.setAvailable(true);
            result.setCheapest(false);
            result.setFastest(false);
            result.setImageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400");
            result.setTags(List.of("premium", "trending"));
            results.add(result);
        }

        BigDecimal minPrice = results.stream()
                .map(VendorSearchResult::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        results.forEach(r -> r.setCheapest(r.getPrice().equals(minPrice)));

        int minEta = results.stream()
                .mapToInt(VendorSearchResult::getEtaMinutes)
                .min()
                .orElse(0);

        results.forEach(r -> r.setFastest(r.getEtaMinutes() == minEta));

        if (results.isEmpty()) {
            log.warn("DoorDash mock search returned no items");
        } else {
            log.info("DoorDash mock search returned {} items", results.size());
        }

        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "DD" + random.nextInt(10000000, 99999999);

        BigDecimal totalAmount = request.getAmount() != null
                ? request.getAmount()
                : request.getItems().stream()
                        .map(VendorOrderRequest.OrderItem::getUnitPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        String trackingUrl = "https://doordash.com/track/" + orderId;

        log.info("DoorDash mock order placed — orderId: {}, total: {}", orderId, totalAmount);

        return VendorOrderResult.success("DoorDash", orderId, totalAmount, trackingUrl);
    }

    private VendorTrackingResult mockTrackOrder(String orderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        VendorTrackingResult.TrackingStatus[] statuses = {
                VendorTrackingResult.TrackingStatus.PREPARING,
                VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
                VendorTrackingResult.TrackingStatus.DELIVERED
        };

        VendorTrackingResult.TrackingStatus status = statuses[random.nextInt(statuses.length)];
        int etaMinutes = random.nextInt(10, 41);

        log.info("DoorDash mock tracking — orderId: {}, status: {}, ETA: {} mins",
                orderId, status, etaMinutes);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(orderId);
        result.setVendorName("DoorDash");
        result.setStatus(status);
        result.setStatusMessage(status.name().replace("_", " ").toLowerCase());
        result.setEtaMinutes(etaMinutes);
        result.setEtaLabel(etaMinutes + " min");
        result.setCurrency("INR");

        return result;
    }
}
