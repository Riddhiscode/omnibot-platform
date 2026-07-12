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
public class BoltAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BoltAdapter.class);

    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;
    private final boolean mockMode;

    public BoltAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
        this.mockMode = vendorProperties.isMockMode();
        log.info("BoltAdapter initialized in {} mode", mockMode ? "MOCK" : "LIVE");
    }

    @Override
    public String getVendorName() {
        return "Bolt";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/bolt.eu";
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
        return mockMode || vendorProperties.getBolt().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!mockMode) {
            log.warn("[Bolt] Live API integration not implemented yet, falling back to mock");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        // Bolt Economy — Affordable everyday rides
        VendorSearchResult economy = new VendorSearchResult();
        economy.setVendorName("Bolt");
        economy.setItemName("Bolt Economy");
        economy.setDescription("Affordable everyday rides");
        economy.setPrice(BigDecimal.valueOf(random.nextInt(55, 121)));
        economy.setCurrency("INR");
        economy.setEtaMinutes(random.nextInt(3, 9));
        economy.setEtaLabel("mins away");
        economy.setRating(Math.round((4.0 + random.nextDouble() * 0.9) * 10.0) / 10.0);
        economy.setReviewCount(random.nextInt(500, 5000));
        economy.setAvailable(true);
        economy.setCheapest(false);
        economy.setFastest(false);
        economy.setTags(List.of("Bolt Economy", "Sedan", "AC", "Everyday"));
        results.add(economy);

        // Bolt Comfort — Newer cars with extra legroom
        VendorSearchResult comfort = new VendorSearchResult();
        comfort.setVendorName("Bolt");
        comfort.setItemName("Bolt Comfort");
        comfort.setDescription("Newer cars with extra legroom");
        comfort.setPrice(BigDecimal.valueOf(random.nextInt(90, 201)));
        comfort.setCurrency("INR");
        comfort.setEtaMinutes(random.nextInt(5, 13));
        comfort.setEtaLabel("mins away");
        comfort.setRating(Math.round((4.2 + random.nextDouble() * 0.7) * 10.0) / 10.0);
        comfort.setReviewCount(random.nextInt(300, 3000));
        comfort.setAvailable(true);
        comfort.setCheapest(false);
        comfort.setFastest(false);
        comfort.setTags(List.of("Bolt Comfort", "Sedan", "Extra Legroom", "Premium"));
        results.add(comfort);

        // Bolt Business — Corporate billing, receipt tracking
        VendorSearchResult business = new VendorSearchResult();
        business.setVendorName("Bolt");
        business.setItemName("Bolt Business");
        business.setDescription("Corporate billing, receipt tracking");
        business.setPrice(BigDecimal.valueOf(random.nextInt(100, 221)));
        business.setCurrency("INR");
        business.setEtaMinutes(random.nextInt(5, 15));
        business.setEtaLabel("mins away");
        business.setRating(Math.round((4.3 + random.nextDouble() * 0.6) * 10.0) / 10.0);
        business.setReviewCount(random.nextInt(200, 2000));
        business.setAvailable(true);
        business.setCheapest(false);
        business.setFastest(false);
        business.setTags(List.of("Bolt Business", "Corporate", "Receipts", "Business"));
        results.add(business);

        // Bolt Scooter — Electric scooter sharing
        VendorSearchResult scooter = new VendorSearchResult();
        scooter.setVendorName("Bolt");
        scooter.setItemName("Bolt Scooter");
        scooter.setDescription("Electric scooter sharing");
        scooter.setPrice(BigDecimal.valueOf(random.nextInt(20, 46)));
        scooter.setCurrency("INR");
        scooter.setEtaMinutes(random.nextInt(1, 6));
        scooter.setEtaLabel("mins away");
        scooter.setRating(Math.round((3.9 + random.nextDouble() * 0.9) * 10.0) / 10.0);
        scooter.setReviewCount(random.nextInt(400, 4000));
        scooter.setAvailable(true);
        scooter.setCheapest(true);
        scooter.setFastest(true);
        scooter.setTags(List.of("Bolt Scooter", "Electric", "Scooter", "Eco-Friendly"));
        results.add(scooter);

        int maxResults = request.getMaxResults() > 0
                ? Math.min(request.getMaxResults(), results.size())
                : results.size();
        return results.subList(0, maxResults);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!mockMode) {
            log.warn("[Bolt] Live order placement not implemented yet, falling back to mock");
        }

        String externalOrderId = "BLT" + String.format("%08d", ThreadLocalRandom.current().nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Bolt",
                externalOrderId,
                request.getAmount(),
                "https://bolt.eu/ride/track/" + externalOrderId
        );
        result.setDriverName("Marco P.");
        result.setDriverPhone("+372 5XX XXX XXX");
        result.setVehicleInfo("Volkswagen Polo | Red | EE 1234");
        result.setEstimatedDelivery("4-10 mins");

        log.debug("Bolt mock order placed — orderId={}", externalOrderId);
        return result;
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!mockMode) {
            log.warn("[Bolt] Live order tracking not implemented yet, falling back to mock");
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
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(7), "Driver Found", "Driver accepted the ride request"));
        if (status != TrackingStatus.DRIVER_ASSIGNED) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(4), "En Route", "Driver heading to pickup"));
        }
        if (status == TrackingStatus.ARRIVED || status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(2), "Driver Arrived", "Driver reached pickup point"));
        }
        if (status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now(), "Ride Started", "Trip in progress"));
        }

        double driverLat = 52.5200 + random.nextDouble(-0.05, 0.05);
        double driverLng = 13.4050 + random.nextDouble(-0.05, 0.05);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Bolt");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(random.nextInt(3, 18));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(55, 220)));
        result.setCurrency("INR");
        result.setDriverLatitude(driverLat);
        result.setDriverLongitude(driverLng);
        result.setDriverName("Marco P.");
        result.setDriverPhone("+372 5XX XXX XXX");
        result.setVehicleInfo("Volkswagen Polo | Red | EE 1234");
        result.setEvents(events);

        log.debug("Bolt mock track — orderId={}, status={}", externalOrderId, status);
        return result;
    }
}
