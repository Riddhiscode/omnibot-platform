package com.omnibot.adapter;

import com.omnibot.adapter.dto.*;
import com.omnibot.adapter.food.*;
import com.omnibot.adapter.transport.*;
import com.omnibot.adapter.shopping.*;
import com.omnibot.config.VendorProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VendorAdaptersTest {

    private VendorProperties props;

    @BeforeEach
    void setUp() {
        props = new VendorProperties();
        props.setMode("mock");
        props.setTimeoutMs(5000);
        props.setMaxRetries(0);
    }

    // ======================== FOOD ========================

    @Test
    void zomatoAdapter_searchReturnsResults() {
        ZomatoAdapter adapter = new ZomatoAdapter(props);
        assertEquals("Zomato", adapter.getVendorName());
        assertEquals(VendorCategory.FOOD, adapter.getCategory());
        assertTrue(adapter.isAvailable());

        VendorSearchRequest req = new VendorSearchRequest("biryani", "Bangalore");
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(r -> r.getVendorName().equals("Zomato")));
        assertTrue(results.stream().allMatch(r -> r.getPrice() != null));
    }

    @Test
    void zomatoAdapter_placeOrderReturnsSuccess() {
        ZomatoAdapter adapter = new ZomatoAdapter(props);
        VendorOrderRequest req = new VendorOrderRequest();
        req.setVendorName("Zomato");
        req.setUserId(1L);
        req.setDeliveryAddress("Koramangala, Bangalore");
        req.setAmount(BigDecimal.valueOf(350));

        VendorOrderResult result = adapter.placeOrder(req);
        assertTrue(result.isSuccess());
        assertNotNull(result.getExternalOrderId());
        assertEquals("Zomato", result.getVendorName());
    }

    @Test
    void zomatoAdapter_trackOrderReturnsStatus() {
        ZomatoAdapter adapter = new ZomatoAdapter(props);
        VendorTrackingResult result = adapter.trackOrder("ZOM-TEST-001");
        assertNotNull(result.getStatus());
        assertEquals("Zomato", result.getVendorName());
        assertEquals("ZOM-TEST-001", result.getExternalOrderId());
    }

    @Test
    void swiggyAdapter_searchReturnsResults() {
        SwiggyAdapter adapter = new SwiggyAdapter(props);
        assertEquals("Swiggy", adapter.getVendorName());
        assertEquals(VendorCategory.FOOD, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("pizza", "Mumbai"));
        assertFalse(results.isEmpty());
    }

    @Test
    void uberEatsAdapter_searchReturnsResults() {
        UberEatsAdapter adapter = new UberEatsAdapter(props);
        assertEquals("UberEats", adapter.getVendorName());
        assertEquals(VendorCategory.FOOD, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("sushi", "Delhi"));
        assertFalse(results.isEmpty());
    }

    @Test
    void doorDashAdapter_searchReturnsResults() {
        DoorDashAdapter adapter = new DoorDashAdapter(props);
        assertEquals("DoorDash", adapter.getVendorName());
        assertEquals(VendorCategory.FOOD, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("burger", "Chennai"));
        assertFalse(results.isEmpty());
    }

    // ======================== TRANSPORT ========================

    @Test
    void uberAdapter_searchReturnsRideTypes() {
        UberAdapter adapter = new UberAdapter(props);
        assertEquals("Uber", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());
        assertEquals("BOOK_RIDE", adapter.getServiceAction());
        assertTrue(adapter.isAvailable());

        VendorSearchRequest req = new VendorSearchRequest();
        req.setQuery("Go");
        req.setLatitude(12.97);
        req.setLongitude(77.59);
        req.setMaxResults(5);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(r -> r.getVendorName().equals("Uber")));
    }

    @Test
    void uberAdapter_trackOrder() {
        UberAdapter adapter = new UberAdapter(props);
        VendorTrackingResult result = adapter.trackOrder("UBR-TEST-001");
        assertNotNull(result.getStatus());
        assertEquals("Uber", result.getVendorName());
    }

    @Test
    void olaAdapter_searchReturnsRideTypes() {
        OlaAdapter adapter = new OlaAdapter(props);
        assertEquals("Ola", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());

        VendorSearchRequest req = new VendorSearchRequest("Mini", "Pune");
        req.setLatitude(18.52);
        req.setLongitude(73.85);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
    }

    @Test
    void lyftAdapter_searchReturnsRideTypes() {
        LyftAdapter adapter = new LyftAdapter(props);
        assertEquals("Lyft", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());

        VendorSearchRequest req = new VendorSearchRequest("Standard", "San Francisco");
        req.setLatitude(37.77);
        req.setLongitude(-122.41);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
    }

    @Test
    void boltAdapter_searchReturnsRideTypes() {
        BoltAdapter adapter = new BoltAdapter(props);
        assertEquals("Bolt", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());

        VendorSearchRequest req = new VendorSearchRequest("Economy", "Tallinn");
        req.setLatitude(59.43);
        req.setLongitude(24.75);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
    }

    @Test
    void rapidoAdapter_searchReturnsRideTypes() {
        RapidoAdapter adapter = new RapidoAdapter(props);
        assertEquals("Rapido", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());

        VendorSearchRequest req = new VendorSearchRequest("Bike", "Hyderabad");
        req.setLatitude(17.38);
        req.setLongitude(78.48);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
    }

    @Test
    void yuluAdapter_searchReturnsRideTypes() {
        YuluAdapter adapter = new YuluAdapter(props);
        assertEquals("Yulu", adapter.getVendorName());
        assertEquals(VendorCategory.TRANSPORT, adapter.getCategory());

        VendorSearchRequest req = new VendorSearchRequest("Move", "Bangalore");
        req.setLatitude(12.97);
        req.setLongitude(77.59);
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
    }

    // ======================== SHOPPING ========================

    @Test
    void amazonAdapter_searchReturnsProducts() {
        AmazonAdapter adapter = new AmazonAdapter(props);
        assertEquals("Amazon", adapter.getVendorName());
        assertEquals(VendorCategory.SHOPPING, adapter.getCategory());
        assertEquals("BUY_NOW", adapter.getServiceAction());
        assertTrue(adapter.isAvailable());

        VendorSearchRequest req = new VendorSearchRequest("laptop", "560001");
        List<VendorSearchResult> results = adapter.search(req);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(r -> r.getPrice() != null));
    }

    @Test
    void amazonAdapter_trackOrder() {
        AmazonAdapter adapter = new AmazonAdapter(props);
        VendorTrackingResult result = adapter.trackOrder("AMZ-TEST-001");
        assertNotNull(result.getStatus());
        assertEquals("Amazon", result.getVendorName());
    }

    @Test
    void flipkartAdapter_searchReturnsProducts() {
        FlipkartAdapter adapter = new FlipkartAdapter(props);
        assertEquals("Flipkart", adapter.getVendorName());
        assertEquals(VendorCategory.SHOPPING, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("phone", "110001"));
        assertFalse(results.isEmpty());
    }

    @Test
    void meeshoAdapter_searchReturnsProducts() {
        MeeshoAdapter adapter = new MeeshoAdapter(props);
        assertEquals("Meesho", adapter.getVendorName());
        assertEquals(VendorCategory.SHOPPING, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("kurti", "400001"));
        assertFalse(results.isEmpty());
    }

    @Test
    void myntraAdapter_searchReturnsProducts() {
        MyntraAdapter adapter = new MyntraAdapter(props);
        assertEquals("Myntra", adapter.getVendorName());
        assertEquals(VendorCategory.SHOPPING, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("sneakers", "560001"));
        assertFalse(results.isEmpty());
    }

    @Test
    void ebayAdapter_searchReturnsProducts() {
        EbayAdapter adapter = new EbayAdapter(props);
        assertEquals("eBay", adapter.getVendorName());
        assertEquals(VendorCategory.SHOPPING, adapter.getCategory());

        List<VendorSearchResult> results = adapter.search(new VendorSearchRequest("watch", "10001"));
        assertFalse(results.isEmpty());
    }

    // ======================== ALL ADAPTERS CONTRACT ========================

    @Test
    void allAdaptersImplementInterfaceCorrectly() {
        List<VendorAdapter> adapters = List.of(
            new ZomatoAdapter(props), new SwiggyAdapter(props),
            new UberEatsAdapter(props), new DoorDashAdapter(props),
            new UberAdapter(props), new OlaAdapter(props),
            new LyftAdapter(props), new BoltAdapter(props),
            new RapidoAdapter(props), new YuluAdapter(props),
            new AmazonAdapter(props), new FlipkartAdapter(props),
            new MeeshoAdapter(props), new MyntraAdapter(props),
            new EbayAdapter(props)
        );

        assertEquals(15, adapters.size());

        for (VendorAdapter adapter : adapters) {
            assertNotNull(adapter.getVendorName(), adapter + " has null name");
            assertNotNull(adapter.getLogoUrl(), adapter + " has null logo");
            assertNotNull(adapter.getCategory(), adapter + " has null category");
            assertNotNull(adapter.getServiceAction(), adapter + " has null action");
            assertTrue(adapter.search(new VendorSearchRequest("test", "test")).size() > 0,
                    adapter + " search returned empty");
        }
    }
}
