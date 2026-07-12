package com.omnibot.adapter.transport;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.adapter.dto.VendorTrackingResult.TrackingEvent;
import com.omnibot.adapter.dto.VendorTrackingResult.TrackingStatus;
import com.omnibot.config.VendorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LyftAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LyftAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    public LyftAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("LyftAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "Lyft";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/lyft.com";
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
        return mockMode || vendorProperties.getLyft().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!mockMode) {
            log.warn("[Lyft] Live API integration not implemented yet, falling back to mock");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        // Lyft Standard — Affordable rides, all day
        VendorSearchResult standard = new VendorSearchResult();
        standard.setVendorName("Lyft");
        standard.setItemName("Lyft Standard");
        standard.setDescription("Affordable rides, all day");
        int standardUSD = random.nextInt(5, 9);
        standard.setPrice(BigDecimal.valueOf(standardUSD * 83));
        standard.setCurrency("INR");
        standard.setEtaMinutes(random.nextInt(5, 13));
        standard.setEtaLabel("mins away");
        standard.setRating(Math.round((4.0 + random.nextDouble() * 0.9) * 10.0) / 10.0);
        standard.setReviewCount(random.nextInt(500, 5000));
        standard.setAvailable(true);
        standard.setCheapest(false);
        standard.setFastest(false);
        standard.setTags(List.of("Lyft Standard", "Sedan", "AC", "Affordable"));
        results.add(standard);

        // Lyft XL — Extra seats for groups
        VendorSearchResult xl = new VendorSearchResult();
        xl.setVendorName("Lyft");
        xl.setItemName("Lyft XL");
        xl.setDescription("Extra seats for groups");
        int xlUSD = random.nextInt(8, 13);
        xl.setPrice(BigDecimal.valueOf(xlUSD * 83));
        xl.setCurrency("INR");
        xl.setEtaMinutes(random.nextInt(7, 16));
        xl.setEtaLabel("mins away");
        xl.setRating(Math.round((4.1 + random.nextDouble() * 0.8) * 10.0) / 10.0);
        xl.setReviewCount(random.nextInt(300, 3000));
        xl.setAvailable(true);
        xl.setCheapest(false);
        xl.setFastest(false);
        xl.setTags(List.of("Lyft XL", "SUV", "6 Seats", "Groups"));
        results.add(xl);

        // Lyft Lux — Premium luxury vehicles
        VendorSearchResult lux = new VendorSearchResult();
        lux.setVendorName("Lyft");
        lux.setItemName("Lyft Lux");
        lux.setDescription("Premium luxury vehicles");
        int luxUSD = random.nextInt(11, 19);
        lux.setPrice(BigDecimal.valueOf(luxUSD * 83));
        lux.setCurrency("INR");
        lux.setEtaMinutes(random.nextInt(8, 19));
        lux.setEtaLabel("mins away");
        lux.setRating(Math.round((4.3 + random.nextDouble() * 0.7) * 10.0) / 10.0);
        lux.setReviewCount(random.nextInt(200, 2000));
        lux.setAvailable(true);
        lux.setCheapest(false);
        lux.setFastest(false);
        lux.setTags(List.of("Lyft Lux", "Luxury", "Premium", "Top-Rated"));
        results.add(lux);

        // Lyft Pink — Bike & scooter shares
        VendorSearchResult pink = new VendorSearchResult();
        pink.setVendorName("Lyft");
        pink.setItemName("Lyft Pink");
        pink.setDescription("Bike & scooter shares");
        int pinkUSD = random.nextInt(5, 8);
        pink.setPrice(BigDecimal.valueOf(pinkUSD * 83));
        pink.setCurrency("INR");
        pink.setEtaMinutes(random.nextInt(4, 11));
        pink.setEtaLabel("mins away");
        pink.setRating(Math.round((3.9 + random.nextDouble() * 0.9) * 10.0) / 10.0);
        pink.setReviewCount(random.nextInt(400, 4000));
        pink.setAvailable(true);
        pink.setCheapest(true);
        pink.setFastest(true);
        pink.setTags(List.of("Lyft Pink", "Bike", "Scooter", "Eco-Friendly"));
        results.add(pink);

        int maxResults = request.getMaxResults() > 0
                ? Math.min(request.getMaxResults(), results.size())
                : results.size();
        return results.subList(0, maxResults);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!mockMode) {
            log.warn("[Lyft] Live order placement not implemented yet, falling back to mock");
        }

        String externalOrderId = "LYFT" + String.format("%08d", ThreadLocalRandom.current().nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Lyft",
                externalOrderId,
                request.getAmount(),
                "https://www.lyft.com/track/" + externalOrderId
        );
        result.setDriverName("Sarah M.");
        result.setDriverPhone("+1 555-XXX-XXXX");
        result.setVehicleInfo("Toyota Camry | Black | CA 7890");
        result.setEstimatedDelivery("5-12 mins");

        log.debug("Lyft mock order placed — orderId={}", externalOrderId);
        return result;
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!mockMode) {
            log.warn("[Lyft] Live order tracking not implemented yet, falling back to mock");
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
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(10), "Ride Requested", "Looking for nearby drivers"));
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(8), "Driver Found", "Driver accepted the ride request"));
        if (status != TrackingStatus.DRIVER_ASSIGNED) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(5), "En Route", "Driver heading to pickup"));
        }
        if (status == TrackingStatus.ARRIVED || status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(2), "Driver Arrived", "Driver reached pickup point"));
        }
        if (status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now(), "Ride Started", "Trip in progress"));
        }

        double driverLat = 40.7128 + random.nextDouble(-0.05, 0.05);
        double driverLng = -74.0060 + random.nextDouble(-0.05, 0.05);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Lyft");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(random.nextInt(3, 18));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(300, 1500)));
        result.setCurrency("INR");
        result.setDriverLatitude(driverLat);
        result.setDriverLongitude(driverLng);
        result.setDriverName("Sarah M.");
        result.setDriverPhone("+1 555-XXX-XXXX");
        result.setVehicleInfo("Toyota Camry | Black | CA 7890");
        result.setEvents(events);

        log.debug("Lyft mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }
}
