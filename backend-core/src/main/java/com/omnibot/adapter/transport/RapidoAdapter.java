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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RapidoAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RapidoAdapter.class);

    public RapidoAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }



    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    @Override
    public String getVendorName() {
        return "Rapido";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/rapido.bike";
    }

    @Override
    public VendorCategory getCategory() {
        return VendorCategory.TRANSPORT;
    }

    @Override
    public String getServiceAction() {
        return "BOOK_BIKE";
    }

    @Override
    public boolean isAvailable() {
        boolean mockMode = vendorProperties.isMockMode();
        return mockMode || vendorProperties.getRapido().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Rapido live API not implemented — falling back to mock");
        }
        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Rapido live API not implemented — falling back to mock");
        }
        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Rapido live API not implemented — falling back to mock");
        }
        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        String[][] rideData = {
                {"Rapido Bike", "Quick bike taxi — fastest in city", "25", "65", "2", "5"},
                {"Rapido Bike XL", "Bike with extra luggage space", "35", "85", "3", "7"},
                {"Rapido Auto", "Auto-rickshaw booking", "30", "80", "3", "8"}
        };

        String[] tags = {"bike", "fast", "express"};

        for (int i = 0; i < rideData.length; i++) {
            String[] rd = rideData[i];
            VendorSearchResult r = new VendorSearchResult();
            r.setVendorName("Rapido");
            r.setItemName(rd[0]);
            r.setDescription(rd[1]);
            r.setPrice(BigDecimal.valueOf(random.nextInt(Integer.parseInt(rd[2]), Integer.parseInt(rd[3]) + 1)));
            r.setCurrency("INR");
            r.setEtaMinutes(random.nextInt(Integer.parseInt(rd[4]), Integer.parseInt(rd[5]) + 1));
            r.setEtaLabel("mins");
            r.setRating(4.5 - i * 0.2);
            r.setReviewCount(random.nextInt(400, 3000));
            r.setAvailable(true);
            r.setCheapest(i == 2);
            r.setFastest(i == 0);
            r.setTags(List.of(tags[i]));
            results.add(r);
        }

        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String externalOrderId = "RPD" + String.format("%08d", random.nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Rapido",
                externalOrderId,
                request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(random.nextInt(25, 86)),
                "https://rapido.app/track/" + externalOrderId
        );
        result.setDriverName("Ravi T.");
        result.setDriverPhone("+91" + random.nextLong(7000000000L, 9999999999L));
        result.setVehicleInfo("Hero Splendor | Black | TS 09 EF 3456");
        result.setEstimatedDelivery("3-6 mins");
        return result;
    }

    private VendorTrackingResult mockTrackOrder(String externalOrderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        TrackingStatus[] statuses = {
                TrackingStatus.SEARCHING,
                TrackingStatus.DRIVER_ASSIGNED,
                TrackingStatus.DRIVER_EN_ROUTE,
                TrackingStatus.ARRIVED,
                TrackingStatus.IN_PROGRESS,
                TrackingStatus.COMPLETED
        };
        TrackingStatus status = statuses[random.nextInt(statuses.length)];

        double driverLat = 17.3850 + random.nextDouble(-0.01, 0.01);
        double driverLng = 78.4867 + random.nextDouble(-0.01, 0.01);

        List<TrackingEvent> events = new ArrayList<>();
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(random.nextInt(1, 10)),
                "ORDER_PLACED", "Ride request placed successfully"));
        if (random.nextBoolean()) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(random.nextInt(1, 5)),
                    "DRIVER_FOUND", "Nearby captain accepted the ride"));
        }

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Rapido");
        result.setStatus(status);
        result.setStatusMessage("Ride status: " + status.name());
        result.setEtaMinutes(random.nextInt(2, 12));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(25, 86)));
        result.setCurrency("INR");
        result.setDriverLatitude(driverLat);
        result.setDriverLongitude(driverLng);
        result.setDriverName("Ravi T.");
        result.setDriverPhone("+91" + random.nextLong(7000000000L, 9999999999L));
        result.setVehicleInfo("Hero Splendor | Black | TS 09 EF 3456");
        result.setEvents(events);
        return result;
    }
}
