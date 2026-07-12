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
public class UberAdapter implements VendorAdapter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UberAdapter.class);

    private final VendorProperties vendorProperties;

    public UberAdapter(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
    }

    @Override
    public String getVendorName() {
        return "Uber";
    }

    @Override
    public String getLogoUrl() {
        return "https://logo.clearbit.com/uber.com";
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
        return vendorProperties.isMockMode() || vendorProperties.getUber().hasApiKey();
    }

    @Override
    public List<VendorSearchResult> search(VendorSearchRequest request) {
        if (vendorProperties.isLiveMode() && vendorProperties.getUber().hasApiKey()) {
            log.warn("[Uber] Live API integration not implemented yet, falling back to mock");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<VendorSearchResult> results = new ArrayList<>();

        VendorSearchResult uberGo = new VendorSearchResult();
        uberGo.setVendorName("Uber");
        uberGo.setItemName("Uber Go");
        uberGo.setDescription("Compact sedan, 4 seats, air-conditioned economy ride");
        uberGo.setPrice(BigDecimal.valueOf(random.nextInt(70, 151)));
        uberGo.setCurrency("INR");
        uberGo.setEtaMinutes(random.nextInt(3, 11));
        uberGo.setEtaLabel("mins away");
        uberGo.setRating(4.0 + random.nextDouble() * 0.9);
        uberGo.setReviewCount(random.nextInt(500, 5000));
        uberGo.setAvailable(true);
        uberGo.setCheapest(false);
        uberGo.setFastest(false);
        uberGo.setTags(List.of("4 Seats", "AC", "Sedan", "Economy"));
        results.add(uberGo);

        VendorSearchResult uberPremier = new VendorSearchResult();
        uberPremier.setVendorName("Uber");
        uberPremier.setItemName("Uber Premier");
        uberPremier.setDescription("Premium sedan, 4 seats, top-rated drivers, luxury ride");
        uberPremier.setPrice(BigDecimal.valueOf(random.nextInt(120, 251)));
        uberPremier.setCurrency("INR");
        uberPremier.setEtaMinutes(random.nextInt(5, 13));
        uberPremier.setEtaLabel("mins away");
        uberPremier.setRating(4.2 + random.nextDouble() * 0.7);
        uberPremier.setReviewCount(random.nextInt(300, 3000));
        uberPremier.setAvailable(true);
        uberPremier.setCheapest(false);
        uberPremier.setFastest(false);
        uberPremier.setTags(List.of("4 Seats", "AC", "Premium Sedan", "Luxury"));
        results.add(uberPremier);

        VendorSearchResult uberAuto = new VendorSearchResult();
        uberAuto.setVendorName("Uber");
        uberAuto.setItemName("Uber Auto");
        uberAuto.setDescription("Auto-rickshaw, 3 seats, affordable open-air commute");
        uberAuto.setPrice(BigDecimal.valueOf(random.nextInt(30, 81)));
        uberAuto.setCurrency("INR");
        uberAuto.setEtaMinutes(random.nextInt(2, 7));
        uberAuto.setEtaLabel("mins away");
        uberAuto.setRating(4.0 + random.nextDouble() * 0.8);
        uberAuto.setReviewCount(random.nextInt(800, 8000));
        uberAuto.setAvailable(true);
        uberAuto.setCheapest(true);
        uberAuto.setFastest(false);
        uberAuto.setTags(List.of("3 Seats", "Auto", "Open Air", "Budget"));
        results.add(uberAuto);

        VendorSearchResult uberMoto = new VendorSearchResult();
        uberMoto.setVendorName("Uber");
        uberMoto.setItemName("Uber Moto");
        uberMoto.setDescription("Bike taxi, 1 seat, fastest way to beat traffic");
        uberMoto.setPrice(BigDecimal.valueOf(random.nextInt(20, 51)));
        uberMoto.setCurrency("INR");
        uberMoto.setEtaMinutes(random.nextInt(1, 5));
        uberMoto.setEtaLabel("mins away");
        uberMoto.setRating(4.1 + random.nextDouble() * 0.8);
        uberMoto.setReviewCount(random.nextInt(400, 4000));
        uberMoto.setAvailable(true);
        uberMoto.setCheapest(false);
        uberMoto.setFastest(true);
        uberMoto.setTags(List.of("1 Seat", "Bike", "Fast", "Helmet Provided"));
        results.add(uberMoto);

        int maxResults = request.getMaxResults() > 0 ? Math.min(request.getMaxResults(), results.size()) : results.size();
        return results.subList(0, maxResults);
    }

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (vendorProperties.isLiveMode() && vendorProperties.getUber().hasApiKey()) {
            log.warn("[Uber] Live order placement not implemented yet, falling back to mock");
        }

        String externalOrderId = "UBR" + String.format("%08d", ThreadLocalRandom.current().nextInt(10000000, 99999999));

        VendorOrderResult result = VendorOrderResult.success(
                "Uber",
                externalOrderId,
                request.getAmount(),
                "https://www.uber.com/track/" + externalOrderId
        );
        result.setEstimatedDelivery("Within 15 minutes");
        result.setDriverName("Amit S.");
        result.setDriverPhone("+91 98XXX XXXXX");
        result.setVehicleInfo("Maruti Swift Dzire | White | MH 12 AB 1234");
        return result;
    }

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (vendorProperties.isLiveMode() && vendorProperties.getUber().hasApiKey()) {
            log.warn("[Uber] Live order tracking not implemented yet, falling back to mock");
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
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(5), "En Route", "Driver is heading to pickup"));
        }
        if (status == TrackingStatus.ARRIVED || status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now().minusMinutes(2), "Driver Arrived", "Driver reached pickup point"));
        }
        if (status == TrackingStatus.IN_PROGRESS) {
            events.add(new TrackingEvent(LocalDateTime.now(), "Ride Started", "Trip in progress"));
        }

        double driverLat = 19.0760 + random.nextDouble(-0.05, 0.05);
        double driverLng = 72.8777 + random.nextDouble(-0.05, 0.05);

        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(externalOrderId);
        result.setVendorName("Uber");
        result.setStatus(status);
        result.setStatusMessage(statusMessage);
        result.setEtaMinutes(random.nextInt(3, 18));
        result.setEtaLabel("mins");
        result.setCurrentFare(BigDecimal.valueOf(random.nextInt(80, 300)));
        result.setCurrency("INR");
        result.setDriverLatitude(driverLat);
        result.setDriverLongitude(driverLng);
        result.setDriverName("Amit S.");
        result.setDriverPhone("+91 98XXX XXXXX");
        result.setVehicleInfo("Maruti Swift Dzire | White | MH 12 AB 1234");
        result.setEvents(events);
        return result;
    }
}
