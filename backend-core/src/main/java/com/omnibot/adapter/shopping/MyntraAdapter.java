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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class MyntraAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyntraAdapter.class);

    public MyntraAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }



    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    private static final String[][] MOCK_PRODUCTS = {
            {"Levi's 511 Slim Fit Jeans", "Stretch denim, mid-rise — the iconic slim fit for men"},
            {"H&M Oversized Graphic Tee", "Unisex cotton tee — bold statement print, relaxed fit"},
            {"Roadster Sports Running Shoes", "Lightweight mesh upper — cushioned sole for daily runs"},
            {"W Women's Printed Maxi Dress", "Floral print, A-line silhouette — perfect for brunch"},
            {"Puma RS-X Reinvention Sneakers", "Chunky retro sneakers — bold colorway, rubber outsole"},
            {"Mango Leather Crossbody Bag", "Genuine leather, compact — adjustable strap, gold hardware"},
            {"Adidas Essentials Fleece Jogger", "Soft brushed fleece — elastic waist, tapered leg"},
            {"Jack & Jones Blazer Slim Fit", "Single-breasted blazer — modern slim fit, woven fabric"}
    };

    private static final String VENDOR_NAME = "Myntra";
    private static final String LOGO_URL = "https://logo.clearbit.com/myntra.com";
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
        return vendorProperties.isMockMode() || vendorProperties.getMyntra().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMyntra().hasApiKey()) {
            log.warn("[Myntra] Live API not yet implemented — falling back to mock data");
        }
        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMyntra().hasApiKey()) {
            log.warn("[Myntra] Live order placement not yet implemented — falling back to mock");
        }
        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!vendorProperties.isMockMode() && vendorProperties.getMyntra().hasApiKey()) {
            log.warn("[Myntra] Live order tracking not yet implemented — falling back to mock");
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
            BigDecimal price = BigDecimal.valueOf(random.nextInt(499, 5000))
                    .setScale(2, RoundingMode.HALF_UP);
            double rating = 3.6 + random.nextDouble() * 1.1;
            int reviewCount = random.nextInt(100, 25000);
            int etaDays = random.nextInt(2, 6);
            boolean available = random.nextDouble() > 0.15;

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
            result.setAvailable(available);
            result.setCheapest(i == 0);
            result.setFastest(i == indices.size() - 1);
            result.setTags(List.of("fashion", "myntra-exclusive"));

            if (available && random.nextDouble() > 0.5) {
                result.setPromoText("Flat " + random.nextInt(10, 60) + "% off on this item");
            }

            results.add(result);
        }

        log.debug("[Myntra] Mock search returned {} results for query='{}'", results.size(), request.getQuery());
        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "MYN" + String.format("%08d", random.nextInt(10000000, 99999999));
        String trackingUrl = "https://myntra.com/track/" + orderId;
        BigDecimal amount = request.getAmount() != null
                ? request.getAmount()
                : BigDecimal.valueOf(random.nextInt(499, 5000)).setScale(2, RoundingMode.HALF_UP);

        log.info("[Myntra] Mock order placed: orderId={}, amount={} {}", orderId, amount, CURRENCY);

        VendorOrderResult result = VendorOrderResult.success(VENDOR_NAME, orderId, amount, trackingUrl);
        result.setEstimatedDelivery("2-5 days");
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
                statusMessage = "Order packed at Myntra warehouse";
                etaMinutes = random.nextInt(1440, 4800);
                etaLabel = "2-5 days";
                break;
            case OUT_FOR_DELIVERY:
                statusMessage = "Shipped via Xpressbees";
                etaMinutes = random.nextInt(90, 360);
                etaLabel = "1.5-6 hours";
                break;
            case DELIVERED:
                statusMessage = "Delivered";
                etaMinutes = 0;
                etaLabel = "Delivered";
                break;
            default:
                statusMessage = "Processing";
                etaMinutes = random.nextInt(1440, 4800);
                etaLabel = "2-5 days";
        }

        List<VendorTrackingResult.TrackingEvent> events = new ArrayList<>();
        events.add(new VendorTrackingResult.TrackingEvent(
                LocalDateTime.now().minusDays(random.nextInt(1, 3)),
                "ORDER_PLACED",
                "Order confirmed by Myntra"
        ));
        if (pick >= 1) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusDays(random.nextInt(0, 2)),
                    "SHIPPED",
                    "Package shipped via Xpressbees — tracking id: XPR" + random.nextInt(100000, 999999)
            ));
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusHours(random.nextInt(1, 6)),
                    "IN_TRANSIT",
                    "Package in transit — last scanned at hub"
            ));
        }
        if (pick >= 2) {
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now().minusHours(random.nextInt(0, 2)),
                    "OUT_FOR_DELIVERY",
                    "Out for delivery — rider en route"
            ));
            events.add(new VendorTrackingResult.TrackingEvent(
                    LocalDateTime.now(),
                    "DELIVERED",
                    "Package delivered successfully"
            ));
        }

        BigDecimal fare = BigDecimal.valueOf(random.nextInt(499, 5000)).setScale(2, RoundingMode.HALF_UP);

        log.debug("[Myntra] Mock tracking for orderId={}: status={}", externalOrderId, status);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName(VENDOR_NAME);
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(etaMinutes);
        result.setEtaLabel(etaLabel);
        result.setCurrentFare(fare);
        result.setCurrency(CURRENCY);
        result.setEvents(events);
        return result;
    }
}
