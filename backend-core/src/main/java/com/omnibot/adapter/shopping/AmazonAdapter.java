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
public class AmazonAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AmazonAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    private static final String[][] MOCK_PRODUCTS = {
        {"Samsung Galaxy S24 Ultra", "12GB RAM, 256GB, Titanium Black — Flagship smartphone"},
        {"Apple MacBook Air M3", "15-inch, 16GB RAM, 512GB SSD — Ultralight powerhouse"},
        {"Sony WH-1000XM5", "Wireless Noise Cancelling Headphones — Industry-leading ANC"},
        {"Nike Air Max 270", "Men's Running Shoes — Max Air cushioning for all-day comfort"},
        {"OnePlus Nord CE4", "8GB RAM, 128GB — Smooth performance at mid-range price"},
        {"boAt Rockerz 551", "Over-Ear Wireless Headphones — 20H playback, boAt signature sound"},
        {"Apple iPad 10th Gen", "10.9-inch, A14 Bionic, 64GB — Your new everyday computer"},
        {"Samsung 55\" Crystal 4K TV", "UA55T5310 — Crystal display, HDR, smart TV"}
    };

    public AmazonAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("AmazonAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "Amazon";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/amazon.in";
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
        return vendorProperties.getAmazon() != null && vendorProperties.getAmazon().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        log.debug("Amazon search — query={}, location={}",
                request != null ? request.getQuery() : null,
                request != null ? request.getLocation() : null);

        if (!isAvailable()) {
            log.warn("Amazon search unavailable — no API key configured");
            return Collections.emptyList();
        }

        if (!mockMode) {
            log.warn("Live Amazon API not yet configured — falling back to mock");
        }

        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        log.debug("Amazon placeOrder — product={}, amount={}",
                request != null ? request.getProductId() : null,
                request != null ? request.getAmount() : null);

        if (!isAvailable()) {
            return VendorOrderResult.failure("Amazon", "Amazon API key not configured");
        }

        if (request == null || request.getProductId() == null) {
            return VendorOrderResult.failure("Amazon", "Product ID is required");
        }

        if (!mockMode) {
            log.warn("Live Amazon API not yet configured — falling back to mock");
        }

        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        log.debug("Amazon trackOrder — orderId={}", externalOrderId);

        if (externalOrderId == null || externalOrderId.isBlank()) {
            VendorTrackingResult result = new VendorTrackingResult();
            result.setVendorName("Amazon");
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Invalid order ID");
            return result;
        }

        if (!isAvailable()) {
            VendorTrackingResult result = new VendorTrackingResult();
            result.setExternalOrderId(externalOrderId);
            result.setVendorName("Amazon");
            result.setStatus(VendorTrackingResult.TrackingStatus.FAILED);
            result.setStatusMessage("Amazon API key not configured");
            return result;
        }

        if (!mockMode) {
            log.warn("Live Amazon API not yet configured — falling back to mock");
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
            BigDecimal price = BigDecimal.valueOf(random.nextInt(1500, 75001));
            int etaMinutes = random.nextInt(1440, 4321);
            int etaDays = etaMinutes / 1440;

            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName("Amazon");
            result.setItemName(item[0]);
            result.setDescription(item[1]);
            result.setPrice(price);
            result.setCurrency("INR");
            result.setEtaMinutes(etaMinutes);
            result.setEtaLabel(etaDays + " day" + (etaDays > 1 ? "s" : ""));
            result.setRating(Math.round((3.5 + random.nextDouble() * 1.3) * 10.0) / 10.0);
            result.setReviewCount(random.nextInt(50, 5001));
            result.setAvailable(true);
            result.setPromoText(random.nextInt(4) == 0 ? "Prime: Free Delivery" : null);

            List<String> tags = new ArrayList<>();
            if (random.nextInt(3) == 0) tags.add("Amazon Choice");
            if (random.nextInt(4) == 0) tags.add("Prime Eligible");
            if (random.nextInt(5) == 0) tags.add("Best Seller");
            if (random.nextInt(6) == 0) tags.add("Limited Deal");
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

        log.debug("Amazon mock search returned {} items", results.size());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "AMZ" + String.format("%08d", random.nextInt(0, 100_000_000));
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        String trackingUrl = "https://amazon.in/track/" + orderId;

        VendorOrderResult result = VendorOrderResult.success("Amazon", orderId, amount, trackingUrl);
        result.setEstimatedDelivery("1-3 days");

        log.debug("Amazon mock order placed — orderId={}, amount={}", orderId, amount);
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
                statusMessage = "Package at fulfillment center";
                etaMinutes = random.nextInt(1440, 4321);
                break;
            case OUT_FOR_DELIVERY:
                statusMessage = "Out for delivery with BlueDart";
                etaMinutes = random.nextInt(60, 361);
                break;
            default:
                statusMessage = "Delivered to doorstep";
                etaMinutes = 0;
                break;
        }

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Amazon");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(etaMinutes);
        result.setEtaLabel(etaMinutes == 0 ? "Delivered" : (etaMinutes / 1440) + " day" + ((etaMinutes / 1440) > 1 ? "s" : ""));
        result.setCurrency("INR");
        result.setCurrentFare(BigDecimal.ZERO);

        result.setEvents(List.of(
            new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusDays(2),
                    "ORDER_PLACED", "Order confirmed by Amazon"),
            new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusMinutes(etaMinutes > 0 ? etaMinutes / 2 : 0),
                    status.name(), statusMessage)
        ));

        log.debug("Amazon mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }
}
