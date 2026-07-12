package com.omnibot.adapter.transport;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.VendorOrderRequest;
import com.omnibot.adapter.dto.VendorOrderResult;
import com.omnibot.adapter.dto.VendorSearchRequest;
import com.omnibot.adapter.dto.VendorSearchResult;
import com.omnibot.adapter.dto.VendorTrackingResult;
import com.omnibot.adapter.dto.VendorTrackingResult.TrackingEvent;
import com.omnibot.adapter.dto.VendorTrackingResult.TrackingStatus;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OlaAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlaAdapter.class);

    private final VendorProperties vendorProperties;

    public OlaAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
    }

    @Override
    public String getVendorName() {
        return "Ola";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/olacabs.com";
    }

    @Override
    public VendorCategory getCategory() {
        return VendorCategory.TRANSPORT;
    }

    @Override
    public String getServiceAction() {
        return "BOOK_RIDE";
    }

    @Override
    public boolean isAvailable() {
        return vendorProperties.isMockMode() || vendorProperties.getOla().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (vendorProperties.isLiveMode() && vendorProperties.getOla().hasApiKey()) {
            log.warn("[Ola] Live API integration not implemented yet, falling back to mock");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        VendorSearchResult olaMini = new VendorSearchResult();
        olaMini.setVendorName("Ola");
        olaMini.setItemName("Ola Mini");
        olaMini.setDescription("Compact hatchback, 4 seats, affordable city ride");
        olaMini.setPrice(BigDecimal.valueOf(random.nextInt(60, 131)));
        olaMini.setCurrency("INR");
        olaMini.setEtaMinutes(random.nextInt(4, 11));
        olaMini.setEtaLabel("mins away");
        olaMini.setRating(4.0 + random.nextDouble() * 0.9);
        olaMini.setReviewCount(random.nextInt(400, 4000));
        olaMini.setAvailable(true);
        olaMini.setCheapest(false);
        olaMini.setFastest(false);
        olaMini.setTags(List.of("4 Seats", "AC", "Hatchback", "Economy"));
        results.add(olaMini);

        VendorSearchResult olaPrime = new VendorSearchResult();
        olaPrime.setVendorName("Ola");
        olaPrime.setItemName("Ola Prime");
        olaPrime.setDescription("Premium sedan, 4 seats, top-rated drivers, comfortable ride");
        olaPrime.setPrice(BigDecimal.valueOf(random.nextInt(100, 221)));
        olaPrime.setCurrency("INR");
        olaPrime.setEtaMinutes(random.nextInt(5, 15));
        olaPrime.setEtaLabel("mins away");
        olaPrime.setRating(4.2 + random.nextDouble() * 0.7);
        olaPrime.setReviewCount(random.nextInt(250, 2500));
        olaPrime.setAvailable(true);
        olaPrime.setCheapest(false);
        olaPrime.setFastest(false);
        olaPrime.setTags(List.of("4 Seats", "AC", "Sedan", "Premium"));
        results.add(olaPrime);

        VendorSearchResult olaAuto = new VendorSearchResult();
        olaAuto.setVendorName("Ola");
        olaAuto.setItemName("Ola Auto");
        olaAuto.setDescription("Auto-rickshaw, 3 seats, budget-friendly short trips");
        olaAuto.setPrice(BigDecimal.valueOf(random.nextInt(25, 71)));
        olaAuto.setCurrency("INR");
        olaAuto.setEtaMinutes(random.nextInt(3, 8));
        olaAuto.setEtaLabel("mins away");
        olaAuto.setRating(4.0 + random.nextDouble() * 0.8);
        olaAuto.setReviewCount(random.nextInt(600, 6000));
        olaAuto.setAvailable(true);
        olaAuto.setCheapest(true);
        olaAuto.setFastest(false);
        olaAuto.setTags(List.of("3 Seats", "Auto", "Open Air", "Budget"));
        results.add(olaAuto);

        VendorSearchResult olaShare = new VendorSearchResult();
        olaShare.setVendorName("Ola");
        olaShare.setItemName("Ola Share");
        olaShare.setDescription("Shared cab, 4 seats, ride with co-passengers and save more");
        olaShare.setPrice(BigDecimal.valueOf(random.nextInt(40, 91)));
        olaShare.setCurrency("INR");
        olaShare.setEtaMinutes(random.nextInt(8, 16));
        olaShare.setEtaLabel("mins away");
        olaShare.setRating(3.8 + random.nextDouble() * 0.9);
        olaShare.setReviewCount(random.nextInt(300, 3000));
        olaShare.setAvailable(true);
        olaShare.setCheapest(false);
        olaShare.setFastest(false);
        olaShare.setTags(List.of("4 Seats", "AC", "Shared", "Eco-Friendly"));
        results.add(olaShare);

        int maxResults = request.getMaxResults() > 0 ? Math.min(request.getMaxResults(), results.size()) : results.size();
        return results.subList(0, maxResults);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (vendorProperties.isLiveMode() && vendorProperties.getOla().hasApiKey()) {
            log.warn("[Ola] Live order placement not implemented yet, falling back to mock");
        }

        String externalOrderId = "OLA" + String.format("%08d", ThreadLocalRandom.current().nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Ola",
                externalOrderId,
                request.getAmount(),
                "https://www.olacabs.com/track/" + externalOrderId
        );
        result.setEstimatedDelivery("Within 10 minutes");
        result.setDriverName("Vikram R.");
        result.setDriverPhone("+91 98XXX XXXXX");
        result.setVehicleInfo("Hyundai i20 | Silver | KA 01 CD 5678");
        return result;
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (vendorProperties.isLiveMode() && vendorProperties.getOla().hasApiKey()) {
            log.warn("[Ola] Live order tracking not implemented yet, falling back to mock");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        TrackingStatus[] statuses = {
                TrackingStatus.DRIVER_ASSIGNED,
                TrackingStatus.DRIVER_EN_ROUTE,
                TrackingStatus.ARRIVED,
                TrackingStatus.IN_PROGRESS
        };
        TrackingStatus status = statuses[random.nextInt(statuses.length)];

        String statusMessage;
        switch (status) {
            case DRIVER_ASSIGNED:
                statusMessage = "Your driver has been assigned and is heading to the pickup location";
                break;
            case DRIVER_EN_ROUTE:
                statusMessage = "Your driver is on the way to the pickup point";
                break;
            case ARRIVED:
                statusMessage = "Your driver has arrived at the pickup location";
                break;
            case IN_PROGRESS:
                statusMessage = "You are on the way to your destination";
                break;
            default:
                statusMessage = "Ride in progress";
        }

        List<TrackingEvent> events = new ArrayList<>();
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(12), "Ride Requested", "Searching for nearby drivers"));
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(10), "Driver Found", "Driver accepted your request"));
        if (status != TrackingStatus.DRIVER_ASSIGNED) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(6), "En Route", "Driver heading to pickup"));
        }
        if (status == TrackingStatus.ARRIVED || status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(3), "Driver Arrived", "Reached pickup point"));
        }
        if (status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now(), "Trip Started", "On the way to destination"));
        }

        double driverLat = 28.6139 + random.nextDouble(-0.05, 0.05);
        double driverLng = 77.2090 + random.nextDouble(-0.05, 0.05);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Ola");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(random.nextInt(4, 20));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(60, 280)));
        result.setCurrency("INR");
        result.setDriverLatitude(driverLat);
        result.setDriverLongitude(driverLng);
        result.setDriverName("Vikram R.");
        result.setDriverPhone("+91 98XXX XXXXX");
        result.setVehicleInfo("Hyundai i20 | Silver | KA 01 CD 5678");
        result.setEvents(events);
        return result;
    }
}
