package com.omnibot.adapter.shopping;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class EbayAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EbayAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    public EbayAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }

    private static final String[][] MOCK_PRODUCTS = {
        {"Apple iPhone 15 Pro Max (Refurbished)", "256GB, Natural Titanium — Certified refurbished, 1-year warranty"},
        {"Vintage Polaroid SX-70 Camera", "Working condition — iconic instant camera from the 1970s"},
        {"Nintendo Switch OLED Bundle", "Console + 3 games + carrying case — limited bundle deal"},
        {"Canon EOS R6 Mark II Body", "Full-frame mirrorless camera — 24.2MP, 40fps burst"},
        {"Dyson V15 Detect Absolute", "Cordless vacuum with laser dust detection — refurbished"},
        {"LEGO Star Wars UCS Millennium Falcon", "7,541 pieces — the ultimate collector's set"},
        {"Bose QuietComfort Ultra Earbuds", "Immersive spatial audio — noise cancelling, premium sound"},
        {"Vintage Rolex Submariner 16610", "Pre-owned, authenticated — classic dive watch"}
    };

    @Override
    public String getVendorName() {
        return "eBay";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/ebay.com";
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
        boolean mockMode = vendorProperties.isMockMode();
        return mockMode || vendorProperties.getEbay().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("eBay live mode not implemented — falling back to mock");
        }
        return List.of(mockSearch(request));
    }

    private VendorSearchResult mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int index = random.nextInt(MOCK_PRODUCTS.length);
        String[] product = MOCK_PRODUCTS[index];

        BigDecimal price = BigDecimal.valueOf(random.nextInt(999, 185001));
        int etaDays = random.nextInt(3, 11);
        double rating = 3.8 + random.nextDouble() * 1.1;

        List<String> tags = new ArrayList<>();
        tags.add("international");
        if (product[0].contains("Refurbished") || product[0].contains("Pre-owned")) {
            tags.add("refurbished");
        }
        if (product[0].contains("Vintage")) {
            tags.add("rare");
        }

        VendorSearchResult result = new VendorSearchResult();
        result.setVendorName(getVendorName());
        result.setItemName(product[0]);
        result.setDescription(product[1]);
        result.setPrice(price);
        result.setCurrency("INR");
        result.setEtaMinutes(etaDays * 24 * 60);
        result.setEtaLabel(etaDays + "-" + (etaDays + 2) + " days");
        result.setRating(Math.round(rating * 10.0) / 10.0);
        result.setReviewCount(random.nextInt(10, 5001));
        result.setImageUrl("https://picsum.photos/seed/" + index + "/300");
        result.setAvailable(true);
        result.setCheapest(random.nextBoolean());
        result.setFastest(random.nextBoolean());
        result.setPromoText(random.nextBoolean() ? "Up to 40% off refurbished" : null);
        result.setTags(tags);
        return result;
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("eBay live mode not implemented — falling back to mock");
        }
        return mockPlaceOrder(request);
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String orderId = "EBY" + String.format("%08d", random.nextInt(10000000, 100000000));
        String trackingUrl = "https://ebay.com/track/" + orderId;

        return VendorOrderResult.success(
            getVendorName(),
            orderId,
            request.getAmount(),
            trackingUrl
        );
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!vendorProperties.isMockMode()) {
            log.warn("eBay live mode not implemented — falling back to mock");
        }
        return mockTrackOrder(externalOrderId);
    }

    private VendorTrackingResult mockTrackOrder(String orderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        LocalDateTime now = LocalDateTime.now();

        VendorTrackingResult.TrackingStatus[] statuses = {
            VendorTrackingResult.TrackingStatus.PREPARING,
            VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
            VendorTrackingResult.TrackingStatus.DELIVERED
        };
        VendorTrackingResult.TrackingStatus status = statuses[random.nextInt(statuses.length)];

        String[] statusMessages = {
            "Seller dispatched the item",
            "In transit via international courier",
            "Cleared customs",
            "Out for delivery",
            "Delivered"
        };
        String statusMessage = statusMessages[random.nextInt(statusMessages.length)];

        List<VendorTrackingResult.TrackingEvent> events = new ArrayList<>();
        events.add(new VendorTrackingResult.TrackingEvent(
            now.minusDays(random.nextInt(1, 6)),
            "ORDER_PLACED",
            "Order confirmed by seller"
        ));
        events.add(new VendorTrackingResult.TrackingEvent(
            now.minusDays(random.nextInt(0, 3)),
            "DISPATCHED",
            "Seller dispatched the item"
        ));
        events.add(new VendorTrackingResult.TrackingEvent(
            now.minusHours(random.nextInt(0, 12)),
            status.name(),
            statusMessage
        ));

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(orderId);
        result.setVendorName(getVendorName());
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(status == VendorTrackingResult.TrackingStatus.DELIVERED ? 0 : random.nextInt(1, 8) * 24 * 60);
        result.setEtaLabel(status == VendorTrackingResult.TrackingStatus.DELIVERED ? "Delivered" : random.nextInt(1, 8) + " days");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(999, 185001)));
        result.setCurrency("INR");
        result.setEvents(events);
        return result;
    }
}