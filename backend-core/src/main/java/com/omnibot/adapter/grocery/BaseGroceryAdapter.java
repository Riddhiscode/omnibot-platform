package com.omnibot.adapter.grocery;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Abstract base for grocery adapters (Phase 3).
 *
 * Encapsulates the safe fallback pattern:
 *   1. If mode=mock → return mock data immediately.
 *   2. If mode=live → attempt real HTTP call via RestTemplate.
 *   3. On ANY exception (timeout, 4xx, 5xx, parse error) →
 *      log a warning and fall back to mock data silently.
 *      The caller never sees a stack trace or 500 error.
 *
 * Concrete subclasses implement:
 *   - buildSearchUrl(VendorSearchRequest) → String
 *   - buildSearchHeaders()               → HttpHeaders
 *   - parseSearchResponse(Map)           → List<VendorSearchResult>
 *   - mockItems()                        → String[][] (name, description, category)
 *   - vendorKey()                        → VendorProperties.VendorKey
 */
public abstract class BaseGroceryAdapter implements VendorAdapter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final VendorProperties props;
    protected final RestTemplate restTemplate;

    protected BaseGroceryAdapter(VendorProperties props) {
        this.props = props;
        // Configure RestTemplate with read + connect timeouts
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeoutMs());
        factory.setReadTimeout(props.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    // ── VendorAdapter contract ────────────────────────────────────────

    @Override
    public VendorCategory getCategory() { return VendorCategory.GROCERY; }

    @Override
    public String getServiceAction() { return "ORDER_GROCERY"; }

    @Override
    public boolean isAvailable() {
        return props.isMockMode() || vendorKey().hasApiKey();
    }

    // ── Search — mock OR live with automatic fallback ─────────────────

    @Override
    public final List<VendorSearchResult> search(VendorSearchRequest request) {
        if (props.isMockMode()) {
            return mockSearch(request);
        }
        if (!vendorKey().hasApiKey()) {
            log.warn("{} search skipped — no API key configured", getVendorName());
            return mockSearch(request);   // graceful degradation
        }
        return liveSearchWithFallback(request);
    }

    /**
     * Makes the real HTTP call. On ANY failure falls back to mock data.
     * Uses Map<String, Object> for JSON parsing — no custom libraries needed.
     */
    @SuppressWarnings("unchecked")
    private List<VendorSearchResult> liveSearchWithFallback(VendorSearchRequest request) {
        String url = null;
        try {
            url = buildSearchUrl(request);
            log.info("{} live search — GET {}", getVendorName(), url);

            HttpHeaders headers = buildSearchHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<VendorSearchResult> results = parseSearchResponse(response.getBody());
                log.info("{} live search returned {} items", getVendorName(), results.size());
                return results;
            }

            log.warn("{} live search returned status {} — falling back to mock",
                    getVendorName(), response.getStatusCode());

        } catch (org.springframework.web.client.ResourceAccessException ex) {
            // Timeout or network unreachable
            log.warn("{} API timed out (url={}) — falling back to mock. Reason: {}",
                    getVendorName(), url, ex.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // 4xx from partner API (bad key, rate-limit, etc.)
            log.warn("{} API client error {} — falling back to mock",
                    getVendorName(), ex.getStatusCode());
        } catch (org.springframework.web.client.HttpServerErrorException ex) {
            // 5xx from partner API (their server down)
            log.warn("{} API server error {} — falling back to mock",
                    getVendorName(), ex.getStatusCode());
        } catch (Exception ex) {
            // JSON parse error, NullPointerException in parseSearchResponse, etc.
            log.warn("{} unexpected error during live search — falling back to mock. Cause: {}",
                    getVendorName(), ex.getMessage());
        }

        return mockSearch(request);   // ← safe fallback
    }

    // ── placeOrder — mock OR live with fallback ───────────────────────

    @Override
    public VendorOrderResult placeOrder(VendorOrderRequest request) {
        if (props.isMockMode() || !vendorKey().hasApiKey()) {
            return mockPlaceOrder(request);
        }
        try {
            return livePlaceOrder(request);
        } catch (Exception ex) {
            log.warn("{} placeOrder failed — falling back to mock. Cause: {}",
                    getVendorName(), ex.getMessage());
            return mockPlaceOrder(request);
        }
    }

    // ── trackOrder — mock OR live with fallback ───────────────────────

    @Override
    public VendorTrackingResult trackOrder(String externalOrderId) {
        if (props.isMockMode() || !vendorKey().hasApiKey()) {
            return mockTrackOrder(externalOrderId);
        }
        try {
            return liveTrackOrder(externalOrderId);
        } catch (Exception ex) {
            log.warn("{} trackOrder failed — falling back to mock. Cause: {}",
                    getVendorName(), ex.getMessage());
            return mockTrackOrder(externalOrderId);
        }
    }

    // ── Shared mock implementations ───────────────────────────────────

    protected List<VendorSearchResult> mockSearch(VendorSearchRequest request) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        String[][] items = mockItems();
        List<String[]> pool = new ArrayList<>(Arrays.asList(items));
        Collections.shuffle(pool, new Random(rng.nextLong()));

        int count = Math.min(rng.nextInt(4, 8), pool.size());
        if (request != null && request.getMaxResults() != null) {
            count = Math.min(count, request.getMaxResults());
        }

        List<VendorSearchResult> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String[] item = pool.get(i);
            VendorSearchResult r = new VendorSearchResult();
            r.setVendorName(getVendorName());
            r.setItemName(item[0]);
            r.setDescription(item[1]);
            r.setPrice(BigDecimal.valueOf(rng.nextInt(30, 500)));
            r.setCurrency("INR");
            r.setEtaMinutes(rng.nextInt(10, 61));
            r.setEtaLabel(r.getEtaMinutes() + " mins");
            r.setRating(rng.nextDouble(3.5, 4.9));
            r.setAvailable(true);
            r.setTags(rng.nextBoolean() ? List.of("Fresh", "Daily Delivered") : List.of());
            results.add(r);
        }
        log.debug("{} mock search returned {} items", getVendorName(), results.size());
        return results;
    }

    protected VendorOrderResult mockPlaceOrder(VendorOrderRequest request) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        String prefix = getVendorName().toUpperCase().replaceAll("[^A-Z]", "").substring(0, 3);
        String orderId = prefix + String.format("%08d", rng.nextInt(0, 100_000_000));
        BigDecimal deliveryFee = BigDecimal.valueOf(rng.nextInt(0, 40));
        BigDecimal total = (request != null && request.getAmount() != null)
                ? request.getAmount().add(deliveryFee)
                : deliveryFee;
        String trackUrl = vendorKey().getEndpoint() + "/track/" + orderId;
        VendorOrderResult result = VendorOrderResult.success(getVendorName(), orderId, total, trackUrl);
        result.setEstimatedDelivery(rng.nextInt(30, 90) + " mins");
        return result;
    }

    protected VendorTrackingResult mockTrackOrder(String orderId) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        VendorTrackingResult.TrackingStatus[] statuses = {
            VendorTrackingResult.TrackingStatus.PREPARING,
            VendorTrackingResult.TrackingStatus.OUT_FOR_DELIVERY,
            VendorTrackingResult.TrackingStatus.DELIVERED
        };
        VendorTrackingResult.TrackingStatus status = statuses[rng.nextInt(statuses.length)];
        VendorTrackingResult result = new VendorTrackingResult();
        result.setExternalOrderId(orderId);
        result.setVendorName(getVendorName());
        result.setStatus(status);
        result.setStatusMessage(status.name().replace('_', ' '));
        result.setEtaMinutes(rng.nextInt(5, 40));
        result.setEtaLabel(result.getEtaMinutes() + " mins");
        return result;
    }

    // ── Abstract hooks — subclasses fill these in ─────────────────────

    /** The VendorKey for this adapter (bigbasket, jiomart, etc.) */
    protected abstract VendorProperties.VendorKey vendorKey();

    /** Build the live search URL from the request. */
    protected abstract String buildSearchUrl(VendorSearchRequest request);

    /** Build HTTP headers (Authorization, Content-Type, etc.) */
    protected abstract HttpHeaders buildSearchHeaders();

    /**
     * Parse the raw JSON response body (already deserialized to Map by RestTemplate)
     * into a list of VendorSearchResult. Use only standard Map/List operations.
     */
    protected abstract List<VendorSearchResult> parseSearchResponse(Map<String, Object> body);

    /**
     * Provide the mock item catalogue for this vendor.
     * Each entry is a String[2]: { itemName, description }.
     */
    protected abstract String[][] mockItems();

    /** Live order placement — override in subclass. */
    protected VendorOrderResult livePlaceOrder(VendorOrderRequest request) {
        log.info("{} live placeOrder — not yet implemented, using mock", getVendorName());
        return mockPlaceOrder(request);
    }

    /** Live order tracking — override in subclass. */
    protected VendorTrackingResult liveTrackOrder(String orderId) {
        log.info("{} live trackOrder — not yet implemented, using mock", getVendorName());
        return mockTrackOrder(orderId);
    }
}
