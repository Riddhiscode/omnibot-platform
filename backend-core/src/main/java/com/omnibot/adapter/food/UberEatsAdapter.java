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
public class UberEatsAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UberEatsAdapter.class);

    private static final String VENDOR_NAME = "UberEats";
    private static final String CURRENCY = "INR";

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    public UberEatsAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("UberEatsAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/ubereats.com";
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
        return vendorProperties.getUbereats() != null
                && vendorProperties.getUbereats().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.info("UberEats search request: query={}, location={}",
                request != null ? request.getQuery() : "null",
                request != null ? request.getLocation() : "null");

        if (!isAvailable()) {
            log.warn("UberEats is not available — cannot perform search");
            return Collections.emptyList();
        }

        if (!mockMode) {
            // TODO: Implement real UberEats API call
            // 1. Build UberEats search request with cuisine, lat/lng, price range
            // 2. Send HTTP GET to UberEats catalog/search endpoint
            // 3. Parse response into VendorSearchResult list
            log.warn("Live UberEats API not yet configured — falling back to mock");
        }

        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.info("UberEats placeOrder request for vendor: {}",
                request != null ? request.getVendorName() : "null");

        if (!isAvailable()) {
            log.warn("UberEats is not available — cannot place order");
            return VendorOrderResult.failure(VENDOR_NAME, "UberEats is not available");
        }

        if (request == null) {
            log.warn("Cannot place order with null request");
            return VendorOrderResult.failure(VENDOR_NAME, "Order request cannot be null");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("Cannot place order with null or empty items");
            return VendorOrderResult.failure(VENDOR_NAME, "Order must contain at least one item");
        }

        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            log.warn("Cannot place order without a delivery address");
            return VendorOrderResult.failure(VENDOR_NAME, "Delivery address is required");
        }

        if (!mockMode) {
            // TODO: Implement real UberEats order placement
            // 1. Validate items are still available on UberEats
            // 2. Calculate final price including surge, taxes, and delivery
            // 3. POST to UberEats order API with payment token
            // 4. Return confirmation with estimated delivery window
            log.warn("Live UberEats API not yet configured — falling back to mock");
        }

        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.info("UberEats trackOrder request for orderId: {}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            log.warn("Cannot track order with null or blank orderId");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName(VENDOR_NAME);
            return result;
        }

        if (!isAvailable()) {
            log.warn("UberEats is not available — cannot track order");
            VendorTrackingResult result = new VendorTrackingResult();
            result.setExternalOrderId(externalOrderId);
            result.setVendorName(VENDOR_NAME);
            return result;
        }

        if (!mockMode) {
            // TODO: Implement real UberEats order tracking
            // 1. GET from UberEats order status endpoint
            // 2. Parse delivery partner details, ETA, live location
            // 3. Return structured tracking response
            log.warn("Live UberEats API not yet configured — falling back to mock");
        }

        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        List<String[]> menuItems = Arrays.asList(
                new String[]{"Classic Cheeseburger", "Juicy beef patty with cheddar, lettuce, tomato, and secret sauce"},
                new String[]{"Margherita Pizza", "Hand-tossed pizza with fresh mozzarella, basil, and San Marzano tomato sauce"},
                new String[]{"Grilled Chicken Wrap", "Herb-marinated chicken with hummus, greens, and roasted peppers"},
                new String[]{"Pad Thai Noodles", "Stir-fried rice noodles with shrimp, peanuts, and tangy tamarind sauce"},
                new String[]{"Caesar Salad", "Crisp romaine, parmesan crisps, croutons, and house-made Caesar dressing"},
                new String[]{"Mushroom Risotto", "Creamy arborio rice with wild mushrooms, truffle oil, and aged parmesan"}
        );

        Collections.shuffle(menuItems, random);
        int itemCount = random.nextInt(4, 7);
        List<VendorSearchResult> results = new ArrayList<>();

        for (int i = 0; i < Math.min(itemCount, menuItems.size()); i++) {
            String[] item = menuItems.get(i);
            String name = item[0];
            String description = item[1];

            BigDecimal price = BigDecimal.valueOf(random.nextInt(150, 501));
            int etaMinutes = random.nextInt(25, 51);
            double rating = 3.9 + random.nextDouble() * 0.7;
            rating = Math.round(rating * 10.0) / 10.0;
            BigDecimal deliveryFee = BigDecimal.valueOf(random.nextInt(30, 71));

            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName(VENDOR_NAME);
            result.setItemName(name);
            result.setDescription(description);
            result.setPrice(price);
            result.setCurrency(CURRENCY);
            result.setEtaMinutes(etaMinutes);
            result.setEtaLabel(etaMinutes + "–" + (etaMinutes + 10) + " min");
            result.setRating(rating);
            result.setReviewCount(random.nextInt(50, 800));
            result.setAvailable(true);
            result.setImageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400");
            result.setTags(Arrays.asList("UberEats", "Premium", deliveryFee.intValue() <= 40 ? "Free delivery" : "Delivery fee applies"));

            results.add(result);
        }

        log.info("UberEats mock search returned {} items", results.size());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "UE" + random.nextInt(10000000, 99999999);

        BigDecimal totalAmount = request.getAmount() != null
                ? request.getAmount()
                : request.getItems().stream()
                        .map(VendorOrderRequest.OrderItem::getUnitPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        String trackingUrl = "https://ubereats.com/track/" + orderId;

        log.info("UberEats mock order placed — orderId: {}, amount: {} INR", orderId, totalAmount);

        return VendorOrderResult.success(VENDOR_NAME, orderId, totalAmount, trackingUrl);
    }

    private VendorTrackingResult mockTrackOrder(String orderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        VendorTrackingResult.TrackingStatus[] statuses = {
                VendorTrackingResult.TrackingStatus.PREPARING,
                VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
                VendorTrackingResult.TrackingStatus.DELIVERED
        };
        VendorTrackingResult.TrackingStatus status = statuses[random.nextInt(statuses.length)];
        int etaMinutes = random.nextInt(8, 36);

        String statusMessage;
        switch (status) {
            case PREPARING:
                statusMessage = "Your food is being prepared by the restaurant";
                break;
            case OUT_FOR_DELIVERY:
                statusMessage = "Your order is on the way — driver is en route";
                break;
            case DELIVERED:
                statusMessage = "Your order has been delivered. Enjoy your meal!";
                etaMinutes = 0;
                break;
            default:
                statusMessage = "Order status: " + status;
        }

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(orderId);
        result.setVendorName(VENDOR_NAME);
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(etaMinutes);
        result.setEtaLabel(etaMinutes > 0 ? etaMinutes + " min" : "Delivered");
        result.setCurrency(CURRENCY);

        List<VendorTrackingResult.TrackingEvent> events = new ArrayList<>();
        events.add(new VendorTrackingResult.TrackingEvent(
                java.time.LocalDateTime.now().minusMinutes(etaMinutes + 10),
                "ORDER_PLACED", "Order confirmed by UberEats"));
        events.add(new VendorTrackingResult.TrackingEvent(
                java.time.LocalDateTime.now().minusMinutes(etaMinutes + 5),
                "PREPARING", "Restaurant started preparing your order"));

        if (status == VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY
                || status == VendorTrackingResult.TrackingStatus.DELIVERED) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now().minusMinutes(etaMinutes),
                    "OUT_FOR_DELIVERY", "Driver picked up your order"));
        }
        if (status == VendorTrackingResult.TrackingStatus.DELIVERED) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    java.time.LocalDateTime.now(),
                    "DELIVERED", "Order delivered successfully"));
        }

        result.setEvents(events);

        log.info("UberEats mock tracking — orderId: {}, status: {}, ETA: {} mins",
                orderId, status, etaMinutes);

        return result;
    }
}
