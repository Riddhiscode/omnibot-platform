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
public class YuluAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(YuluAdapter.class);

    public YuluAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
        this.restTemplate = new RestTemplate();
    }



    private final VendorProperties vendorProperties;
    private final RestTemplate restTemplate;

    @Override
    public String getVendorName() {
        return "Yulu";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/yulu.bike";
    }

    @Override
    public VendorCategory getCategory() {
        return VendorCategory.TRANSPORT;
    }

    @Override
    public String getServiceAction() {
        return "BOOK_CYCLE";
    }

    @Override
    public boolean isAvailable() {
        boolean mockMode = vendorProperties.isMockMode();
        return mockMode || vendorProperties.getYulu().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Yulu live API not implemented — falling back to mock");
        }
        return mockSearch(request);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Yulu live API not implemented — falling back to mock");
        }
        return mockPlaceOrder(request);
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (!vendorProperties.isMockMode()) {
            log.warn("Yulu live API not implemented — falling back to mock");
        }
        return mockTrackOrder(externalOrderId);
    }

    private List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        String[][] rideData = {
                {"Yulu Move", "Electric bicycle — eco-friendly short commutes", "10", "25", "1", "3"},
                {"Yulu Move Lite", "Lightweight e-scooter for quick errands", "8", "18", "1", "2"},
                {"Yulu Miracle", "Electric scooter with longer range", "15", "35", "2", "5"}
        };

        String[][] tagData = {
                {"electric", "bicycle", "eco"},
                {"electric", "scooter", "lite"},
                {"electric", "scooter", "long-range"}
        };

        for (int i = 0; i < rideData.length; i++) {
            String[] rd = rideData[i];
            VendorSearchResult r = new VendorSearchResult();
            r.setVendorName("Yulu");
            r.setItemName(rd[0]);
            r.setDescription(rd[1]);
            r.setPrice(BigDecimal.valueOf(random.nextInt(Integer.parseInt(rd[2]), Integer.parseInt(rd[3]) + 1)));
            r.setCurrency("INR");
            r.setEtaMinutes(random.nextInt(Integer.parseInt(rd[4]), Integer.parseInt(rd[5]) + 1));
            r.setEtaLabel("mins");
            r.setRating(4.4 - i * 0.2);
            r.setReviewCount(random.nextInt(300, 2000));
            r.setAvailable(true);
            r.setCheapest(i == 1);
            r.setFastest(i == 0);
            r.setTags(List.of(tagData[i]));
            results.add(r);
        }

        return results;
    }

    private VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String externalOrderId = "YLU" + String.format("%08d", random.nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Yulu",
                externalOrderId,
                request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(random.nextInt(10, 36)),
                "https://yulu.app/track/" + externalOrderId
        );
        result.setDriverName("Dock Station");
        result.setVehicleInfo("Yulu Move | Green | KA 52 YL 7890");
        result.setEstimatedDelivery("2-4 mins");
        return result;
    }

    private VendorTrackingResult mockTrackOrder(String externalOrderId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        TrackingStatus[] statuses = {
                TrackingStatus.DRIVER_ASSIGNED,
                TrackingStatus.IN_PROGRESS,
                TrackingStatus.COMPLETED
        };
        TrackingStatus status = statuses[random.nextInt(statuses.length)];

        String statusMessage;
        if (status == TrackingStatus.DRIVER_ASSIGNED) {
            statusMessage = "Pick up from nearest Yulu dock";
        } else if (status == TrackingStatus.IN_PROGRESS) {
            statusMessage = "Ride in progress — enjoy your eco-trip";
        } else {
            statusMessage = "Ride completed — thank you for riding green";
        }

        List<TrackingEvent> events = new ArrayList<>();
        events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(random.nextInt(1, 15)),
                "RIDE_BOOKED", "Yulu ride booked at nearest dock"));
        if (status != TrackingStatus.DRIVER_ASSIGNED) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(random.nextInt(1, 8)),
                    "RIDE_STARTED", "Vehicle unlocked and ride started"));
        }
        if (status == TrackingStatus.COMPLETED) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(random.nextInt(0, 3)),
                    "RIDE_ENDED", "Vehicle locked at destination dock"));
        }

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Yulu");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(status == TrackingStatus.COMPLETED ? 0 : random.nextInt(1, 10));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(10, 36)));
        result.setCurrency("INR");
        result.setDriverLatitude(17.3850 + random.nextDouble(-0.005, 0.005));
        result.setDriverLongitude(78.4867 + random.nextDouble(-0.005, 0.005));
        result.setDriverName("Dock Station");
        result.setVehicleInfo("Yulu Move | Green | KA 52 YL 7890");
        result.setEvents(events);
        return result;
    }
}
