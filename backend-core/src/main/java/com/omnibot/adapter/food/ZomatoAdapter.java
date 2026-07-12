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
            result.setEtaMinutes(random.nextInt(20, 46));
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
        log.warn("Live Zomato API not yet configured \u2014 falling back to mock");
        // TODO: Real implementation would call:
        //   GET {endpoint}/restaurants?q={query}&lat={latitude}&lon={longitude}
        //   Headers: "user-key": {apiKey}
        //   Response mapped to VendorSearchResult
        return mockSearch(request);
    }

    private VendorOrderResult livePlaceOrder(VendorOrderRequest request) {
        log.warn("Live Zomato API not yet configured \u2014 falling back to mock");
        // TODO: Real implementation would call:
        //   POST {endpoint}/orders
        //   Headers: "user-key": {apiKey}
        //   Body: { restaurant_id, items, delivery_address, payment_method }
        //   Response mapped to VendorOrderResult
        return mockPlaceOrder(request);
    }

    private VendorTrackingResult liveTrackOrder(String externalOrderId) {
        log.warn("Live Zomato API not yet configured \u2014 falling back to mock");
        // TODO: Real implementation would call:
        //   GET {endpoint}/orders/{externalOrderId}/status
        //   Headers: "user-key": {apiKey}
        //   Response mapped to VendorTrackingResult
        return mockTrackOrder(externalOrderId);
    }
}
