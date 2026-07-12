package com.omnibot.adapter.shopping;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class MeeshoAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeeshoAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    private static final String[][] MOCK_PRODUCTS = {
            {"Women's Cotton Kurti Set", "Elegant A-line kurti with palazzo and dupatta — perfect for daily wear"},
            {"Men's Slim Fit Denim Jeans", "Stretchable slim-fit jeans — dark blue wash, comfortable for all day"},
            {"Kids' Cartoon Print T-Shirt Pack", "Pack of 3 cotton t-shirts — fun prints, soft fabric"},
            {"Ethnic Dupatta Collection", "Handloom cotton dupatta — vibrant colors, block print design"},
            {"Kitchen Storage Containers Set", "Set of 6 airtight BPA-free containers — keep food fresh longer"},
            {"Women's Juttis Collection", "Handcrafted Mojari juttis — embroidered, comfortable flat sole"},
            {"Men's Casual Polo T-Shirt", "Cotton pique polo — classic fit, available in 8 colors"},
            {"Home Decor LED String Lights", "20 LEDs warm white fairy lights — indoor/outdoor decoration"}
    };

    private static final String[] MOCK_IMAGES = {
            "https://images.meesho.com/images/products/1/sample1.jpg",
            "https://images.meesho.com/images/products/2/sample2.jpg",
            "https://images.meesho.com/images/products/3/sample3.jpg",
            "https://images.meesho.com/images/products/4/sample4.jpg",
            "https://images.meesho.com/images/products/5/sample5.jpg",
            "https://images.meesho.com/images/products/6/sample6.jpg",
            "https://images.meesho.com/images/products/7/sample7.jpg",
            "https://images.meesho.com/images/products/8/sample8.jpg"
    };

    public MeeshoAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }

    private static final String VENDOR_NAME = "Meesho";
    private static final String LOGO_URL = "https://logo.clearbit.com/meesho.com";
    private static final String CURRENCY = "INR";

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
        return VendorCategory.SHOPPING;
    }

    @Override
    public String getServiceAction() {
        return "BUY_NOW";
    }

    @Override
    public boolean isAvailable() {
        return vendorProperties.isMockMode() || vendorProperties.getMeesho().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMeesho().hasApiKey()) {
            log.warn("[Meesho] Live API not yet implemented — falling back to mock data");
        }
        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMeesho().hasApiKey()) {
            log.warn("[Meesho] Live order placement not yet implemented — falling back to mock");
        }
        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMeesho().hasApiKey()) {
            log.warn("[Meesho] Live order tracking not yet implemented — falling back to mock");
        }
        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int maxResults = request.getMaxResults() > 0 ? request.getMaxResults() : 5;
        List<VendorSearchResult> results = new ArrayList<>();

        List<Integer> indices = random.ints(0, MOCK_PRODUCTS.length)
                .distinct()
                .limit(Math.min(maxResults, MOCK_PRODUCTS.length))
                .boxed()
                .collect(Collectors.toList());

        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            BigDecimal price = BigDecimal.valueOf(random.nextInt(149, 1300))
                    .setScale(2, RoundingMode.HALF_UP);
            double rating = 3.2 + random.nextDouble() * 1.3;
            int reviewCount = random.nextInt(50, 12000);
            int etaDays = random.nextInt(3, 8);
            boolean available = random.nextBoolean() || random.nextDouble() > 0.2;

            VendorSearchResult result = new VendorSearchResult();
            result.setVendorName(VENDOR_NAME);
            result.setItemName(MOCK_PRODUCTS[idx][0]);
            result.setDescription(MOCK_PRODUCTS[idx][1]);
            result.setPrice(price);
            result.setCurrency(CURRENCY);
            result.setEtaMinutes(etaDays * 24 * 60);
            result.setEtaLabel(etaDays + "-" + (etaDays + 2) + " days");
            result.setRating(Math.round(rating * 10.0) / 10.0);
            result.setReviewCount(reviewCount);
            result.setImageUrl(MOCK_IMAGES[idx]);
            result.setAvailable(available);
            result.setCheapest(i == 0);
            result.setFastest(i == indices.size() - 1);
            result.setTags(List.of("budget-friendly", "meesho-choice"));

            if (available && random.nextDouble() > 0.6) {
                result.setPromoText("Extra ₹" + random.nextInt(20, 150) + " off on first order");
            }

            results.add(result);
        }

        log.debug("[Meesho] Mock search returned {} results for query='{}'", results.size(), request.getQuery());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "MSH" + String.format("%08d", random.nextInt(10000000, 99999999));
        String trackingUrl = "https://meesho.com/track/" + orderId;
        BigDecimal amount = request.getAmount() != null
                ? request.getAmount()
                : BigDecimal.valueOf(random.nextInt(149, 1300)).setScale(2, RoundingMode.HALF_UP);

        log.info("[Meesho] Mock order placed: orderId={}, amount={} {}", orderId, amount, CURRENCY);

        VendorOrderResult result = VendorOrderResult.success(VENDOR_NAME, orderId, amount, trackingUrl);
        result.setCurrency(CURRENCY);
        result.setStatus("CONFIRMED");
        result.setEstimatedDelivery("3-7 days");
        return result;
    }

    private VendorTrackingResult mockTrackOrder(String externalOrderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        VendorTrackingResult.TrackingStatus[] statuses = {
                VendorTrackingResult.TrackingStatus.PREPARING,
                VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
                VendorTrackingResult.TrackingStatus.DELIVERED
        };

        int pick = random.nextInt(statuses.length);
        VendorTrackingResult.TrackingStatus status = statuses[pick];

        String statusMessage;
        int etaMinutes;
        String etaLabel;

        switch (status) {
            case PREPARING:
                statusMessage = "Order confirmed by seller";
                etaMinutes = random.nextInt(2880, 7200);
                etaLabel = "3-7 days";
                break;
            case OUT_FOR_DELIVERY:
                statusMessage = "Shipped via Delhivery";
                etaMinutes = random.nextInt(120, 480);
                etaLabel = "2-8 hours";
                break;
            case DELIVERED:
                statusMessage = "Delivered";
                etaMinutes = 0;
                etaLabel = "Delivered";
                break;
            default:
                statusMessage = "Processing";
                etaMinutes = random.nextInt(2880, 7200);
                etaLabel = "3-7 days";
        }

        List<VendorTrackingResult.TrackingEvent> events = new ArrayList<>();
        events.add(new VendorTrackingResult.TrackingEvent(
                LocalDateTime.now().minusDays(random.nextInt(1, 4)),
                "ORDER_PLACED",
                "Order confirmed by Meesho"
        ));
        if (pick >= 1) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusDays(random.nextInt(0, 2)),
                    "SHIPPED",
                    "Package shipped via Delhivery — tracking id: DLV" + random.nextInt(100000, 999999)
            ));
        }
        if (pick >= 2) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now(),
                    "DELIVERED",
                    "Package delivered successfully"
            ));
        }

        BigDecimal fare = BigDecimal.valueOf(random.nextInt(149, 1300)).setScale(2, RoundingMode.HALF_UP);

        log.debug("[Meesho] Mock tracking for orderId={}: status={}", externalOrderId, status);

        VendorTrackingResult trackingResult = new VendorTrackingResult();
        trackingResult.setExternalOrderId(externalOrderId);
        trackingResult.setVendorName(VENDOR_NAME);
        trackingResult.setStatus(status);
        trackingResult.setStatusMessage(statusMessage);
        trackingResult.setEtaMinutes(etaMinutes);
        trackingResult.setEtaLabel(etaLabel);
        trackingResult.setCurrentFare(fare);
        trackingResult.setCurrency(CURRENCY);
        trackingResult.setEvents(events);
        return trackingResult;
    }
}
