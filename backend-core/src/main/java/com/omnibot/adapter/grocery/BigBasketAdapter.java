package com.omnibot.adapter.grocery;

import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.*;

/**
 * BigBasket vendor adapter — Phase 3 grocery integration.
 *
 * Mock mode (omnibot.vendor.mode=mock):
 *   Returns realistic grocery mock data for development/testing.
 *
 * Live mode (omnibot.vendor.mode=live):
 *   Calls the BigBasket partner API:
 *     GET {endpoint}/catalog/products?q={query}&city={location}&page_size={maxResults}
 *     Header: bb-api-key: {apiKey}
 *
 *   Response shape expected from BigBasket API:
 *   {
 *     "products": [
 *       { "name": "...", "desc": "...", "mrp": 120.0, "sp": 99.0,
 *         "delivery_mins": 30, "rating": 4.2, "unit": "1 kg" }
 *     ]
 *   }
 *
 * On any failure the base class automatically falls back to mock data.
 *
 * To activate:
 *   application.properties:
 *     omnibot.vendor.mode=live
 *     omnibot.vendor.bigbasket.api-key=${BIGBASKET_API_KEY}
 *     omnibot.vendor.bigbasket.endpoint=https://partner-api.bigbasket.com/v1
 */
@Component
public class BigBasketAdapter extends BaseGroceryAdapter {

    private static final String VENDOR_NAME = "BigBasket";
    private static final String LOGO_URL    = "https://logo.clearbit.com/bigbasket.com";

    // ── Realistic mock grocery catalogue ─────────────────────────────
    private static final String[][] MOCK_ITEMS = {
        { "Organic Basmati Rice 5 kg",       "Premium aged basmati rice, naturally fragrant. FSSAI certified organic." },
        { "Farm Fresh Whole Milk 1 L",        "Pasteurised full-cream milk from grass-fed cows, delivered daily." },
        { "Amul Butter 500 g",                "Creamy, salted table butter made from fresh pasteurised cream." },
        { "Fresh Tomatoes 1 kg",              "Vine-ripened locally sourced tomatoes, rich in lycopene." },
        { "Yellow Moong Dal 500 g",           "Husked & split green gram, high protein, quick-cook variety." },
        { "Aashirvaad Whole Wheat Atta 5 kg", "Stone-ground whole wheat flour, retains natural wheat germ & bran." },
        { "Sunflower Oil 1 L (Fortune)",      "Light, heart-healthy cooking oil with high smoke point." },
        { "Free Range Eggs — Tray of 12",     "Eggs from free-range hens, naturally fed, no hormones." },
        { "Baby Spinach 250 g",               "Tender baby spinach leaves, triple-washed and ready to eat." },
        { "Greek Yoghurt 400 g (Epigamia)",   "Thick, creamy yoghurt, high protein, no added sugar." },
        { "Alphonso Mango 6 pc (600 g)",      "Premium Devgad Alphonso mangoes, GI-tagged, naturally ripened." },
        { "Cold-Pressed Coconut Oil 500 ml",  "Virgin coconut oil extracted without heat, retains nutrients." },
    };

    public BigBasketAdapter(VendorProperties props) {
        super(props);
        log.info("BigBasketAdapter initialised in {} mode", props.isMockMode() ? "MOCK" : "LIVE");
    }

    // ── Identity ──────────────────────────────────────────────────────

    @Override public String getVendorName() { return VENDOR_NAME; }
    @Override public String getLogoUrl()    { return LOGO_URL; }

    @Override
    protected VendorProperties.VendorKey vendorKey() {
        return props.getBigbasket();
    }

    // ── Live API wiring ───────────────────────────────────────────────

    @Override
    protected String buildSearchUrl(VendorSearchRequest request) {
        String base = vendorKey().getEndpoint();
        if (base == null || base.isBlank()) base = "https://partner-api.bigbasket.com/v1";

        return UriComponentsBuilder.fromHttpUrl(base + "/catalog/products")
                .queryParam("q",         request.getQuery() != null ? request.getQuery() : "")
                .queryParam("city",      request.getLocation() != null ? request.getLocation() : "")
                .queryParam("page_size", request.getMaxResults() != null ? request.getMaxResults() : 10)
                .toUriString();
    }

    @Override
    protected HttpHeaders buildSearchHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("bb-api-key", vendorKey().getApiKey());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Parse BigBasket API response:
     * { "products": [ { "name", "desc", "sp", "delivery_mins", "rating", "unit" } ] }
     *
     * Uses only standard Map<String,Object> — no custom libraries.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<VendorSearchResult> parseSearchResponse(Map<String, Object> body) {
        List<VendorSearchResult> results = new ArrayList<>();

        Object productsRaw = body.get("products");
        if (!(productsRaw instanceof List)) {
            log.warn("BigBasket response missing 'products' array");
            return results;
        }

        for (Object item : (List<?>) productsRaw) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> p = (Map<String, Object>) item;

            VendorSearchResult r = new VendorSearchResult();
            r.setVendorName(VENDOR_NAME);
            r.setItemName(stringOf(p, "name"));
            r.setDescription(stringOf(p, "desc"));

            // Use selling price (sp); fall back to mrp if sp is missing
            double price = doubleOf(p, "sp");
            if (price == 0) price = doubleOf(p, "mrp");
            r.setPrice(BigDecimal.valueOf(price));
            r.setCurrency("INR");

            int eta = intOf(p, "delivery_mins");
            r.setEtaMinutes(eta > 0 ? eta : 30);
            r.setEtaLabel(r.getEtaMinutes() + " mins");

            r.setRating(doubleOf(p, "rating"));
            r.setAvailable(true);

            String unit = stringOf(p, "unit");
            if (!unit.isBlank()) r.setTags(List.of(unit));

            results.add(r);
        }
        return results;
    }

    @Override
    protected String[][] mockItems() { return MOCK_ITEMS; }

    // ── Parsing helpers (no external libs) ───────────────────────────

    private String stringOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    private double doubleOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0.0;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }

    private int intOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
