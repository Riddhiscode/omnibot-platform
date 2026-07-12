package com.omnibot.adapter.shopping;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FlipkartAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlipkartAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    private static final String[][] MOCK_PRODUCTS = {
        {"Realme GT 6T", "8GB RAM, 256GB — Segment-leading Snapdragon processor"},
        {"HP Pavilion 15", "Intel i7, 16GB RAM, 512GB SSD — Slim business laptop"},
        {"JBL Tune 510BT", "On-Ear Wireless Headphones — Pure Bass sound, 40H battery"},
        {"Puma RS-X Sneakers", "Men's Chunky Sneakers — Bold colors, retro design"},
        {"Xiaomi Redmi Note 13 Pro", "120Hz AMOLED, 200MP Camera — Mid-range champion"},
        {"Boat Airdopes 141", "TWS Earbuds — 42H playback, IPX4 water resistant"},
        {"Lenovo Tab M10 Plus", "3rd Gen, 4GB RAM, 128GB — Family entertainment tablet"},
        {"LG 43\" 4K Smart TV", "43UR73006LA — webOS, AI ThinQ, HDR10 Pro"}
    };

    public FlipkartAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("FlipkartAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "Flipkart";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/flipkart.com";
    }

    @Override
    public VendorCategory getCategory() {
        return VendorCategory.SHOPPING;
    }

    @Override
    public String getServiceAction() {
        return "BUY_NOW";
    }

    @Override
    public boolean isAvailable() {
        if (mockMode) return true;
        return vendorProperties.getFlipkart() != null && vendorProperties.getFlipkart().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.debug("Flipkart search — query={}, location={}",
                request != null ? request.getQuery() : null,
                request != null ? request.getLocation() : null);

        if (!isAvailable()) {
            log.warn("Flipkart search unavailable — no API key configured");
            return Collections.emptyList();
        }

        if (!mockMode) {
            log.warn("Live Flipkart API not yet configured — falling back to mock");
        }

        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.debug("Flipkart placeOrder — product={}, amount={}",
                request != null ? request.getProductId() : null,
                request != null ? request.getAmount() : null);

        if (!isAvailable()) {
            return VendorOrderResult.failure("Flipkart", "Flipkart API key not configured");
        }

        if (request == null || request.getProductId() == null) {
            return VendorOrderResult.failure("Flipkart", "Product ID is required");
        }

        if (!mockMode) {
            log.warn("Live Flipkart API not yet configured — falling back to mock");
        }

        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.debug("Flipkart trackOrder — orderId={}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName("Flipkart");
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Invalid order ID");
            return result;
        }

        if (!isAvailable()) {
            VendorTrackingResult result = new VendorTrackingResult();
            result.setExternalOrderId(externalOrderId);
            result.setVendorName("Flipkart");
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Flipkart API key not configured");
            return result;
        }

        if (!mockMode) {
            log.warn("Live Flipkart API not yet configured — falling back to mock");
        }

        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(4, 7);
        List<String[]> pool = new ArrayList<>(Arrays.asList(MOCK_PRODUCTS));
        Collections.shuffle(pool, random);

        List<VendorSearchResult> results = new ArrayList<>();
        BigDecimal minPrice = null;
        int minEta = Integer.MAX_VALUE;

        for (int i = 0; i < count && i < pool.size(); i++) {
            String[] item = pool.get(i);
            BigDecimal price = BigDecimal.valueOf(random.nextInt(1200, 65001));
            int etaMinutes = random.nextInt(2880, 5761);
            int etaDays = etaMinutes / 1440;

            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName("Flipkart");
            result.setItemName(item[0]);
            result.setDescription(item[1]);
            result.setPrice(price);
            result.setCurrency("INR");
            result.setEtaMinutes(etaMinutes);
            result.setEtaLabel(etaDays + " day" + (etaDays > 1 ? "s" : ""));
            result.setRating(Math.round((3.4 + random.nextDouble() * 1.3) * 10.0) / 10.0);
            result.setReviewCount(random.nextInt(30, 4501));
            result.setAvailable(true);
            result.setPromoText(random.nextInt(4) == 0 ? "Flipkart Assured" : null);

            List<String> tags = new ArrayList<>();
            if (random.nextInt(3) == 0) tags.add("Flipkart Assured");
            if (random.nextInt(4) == 0) tags.add("Bestseller");
            if (random.nextInt(5) == 0) tags.add("SuperCoin Deal");
            if (random.nextInt(6) == 0) tags.add("Top Selling");
            result.setTags(tags);

            results.add(result);

            if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
            if (etaMinutes < minEta) minEta = etaMinutes;
        }

        for (VendorSearchResult r : results) {
            if (r.getPrice().compareTo(minPrice) == 0) r.setCheapest(true);
            if (r.getEtaMinutes() == minEta) r.setFastest(true);
        }

        if (!results.isEmpty() && request != null && request.getMaxPrice() != null) {
            results.removeIf(r -> r.getPrice().compareTo(BigDecimal.valueOf(request.getMaxPrice())) > 0);
        }

        log.debug("Flipkart mock search returned {} items", results.size());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "FLP" + String.format("%08d", random.nextInt(0, 100_000_000));
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        String trackingUrl = "https://flipkart.com/track/" + orderId;

        VendorOrderResult result = VendorOrderResult.success("Flipkart", orderId, amount, trackingUrl);
        result.setEstimatedDelivery("2-4 days");

        log.debug("Flipkart mock order placed — orderId={}, amount={}", orderId, amount);
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

        String statusMessage;
        int etaMinutes;
        switch (status) {
            case PREPARING:
                statusMessage = "Package at Flipkart warehouse";
                etaMinutes = random.nextInt(2880, 5761);
                break;
            case OUT_FOR_DELIVERY:
                statusMessage = "Out for delivery with Ekart Logistics";
                etaMinutes = random.nextInt(120, 481);
                break;
            default:
                statusMessage = "Delivered to doorstep";
                etaMinutes = 0;
                break;
        }

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Flipkart");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(etaMinutes);
        result.setEtaLabel(etaMinutes == 0 ? "Delivered" : (etaMinutes / 1440) + " day" + ((etaMinutes / 1440) > 1 ? "s" : ""));
        result.setCurrency("INR");
        result.setCurrentFare(BigDecimal.ZERO);

        result.setEvents(List.of(
            new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusDays(3),
                    "ORDER_PLACED", "Order confirmed by Flipkart"),
            new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusMinutes(etaMinutes > 0 ? etaMinutes / 2 : 0),
                    status.name(), statusMessage)
        ));

        log.debug("Flipkart mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }
}
