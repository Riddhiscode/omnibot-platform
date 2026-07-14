package com.omnibot.adapter.grocery;

import com.omnibot.adapter.dto.*;
import com.omnibot.config.VendorProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.*;

/**
 * JioMart vendor adapter — Phase 3 grocery integration.
 *
 * Mock mode (omnibot.vendor.mode=mock):
 *   Returns realistic JioMart-flavoured grocery mock data.
 *
 * Live mode (omnibot.vendor.mode=live):
 *   Calls the JioMart partner API:
 *     GET {endpoint}/products/search?keyword={query}&pincode={location}&limit={maxResults}
 *     Headers:
 *       Authorization: Bearer {apiKey}
 *       X-Merchant-Id: {merchantId}
 *
 *   Response shape expected from JioMart API:
 *   {
 *     "data": {
 *       "items": [
 *         { "productName": "...", "shortDesc": "...", "offerPrice": 85.0,
 *           "mrp": 99.0, "deliverySlot": "Tomorrow 9 AM–12 PM",
 *           "avgRating": 4.1, "imageUrl": "https://..." }
 *       ]
 *     }
 *   }
 *
 * On any failure the base class automatically falls back to mock data.
 *
 * To activate:
 *   application.properties:
 *     omnibot.vendor.mode=live
 *     omnibot.vendor.jiomart.api-key=${JIOMART_API_KEY}
 *     omnibot.vendor.jiomart.merchant-id=${JIOMART_MERCHANT_ID}
 *     omnibot.vendor.jiomart.endpoint=https://api.jiomart.com/partner/v1
 */
@Component
public class JioMartAdapter extends BaseGroceryAdapter {

    private static final String VENDOR_NAME = "JioMart";
    private static final String LOGO_URL    = "https://logo.clearbit.com/jiomart.com";

    // ── Realistic mock grocery catalogue ─────────────────────────────
    private static final String[][] MOCK_ITEMS = {
        { "Tata Salt 1 kg",                    "Fine iodised salt with added iodine for healthy nutrition." },
        { "Reliance Fresh Paneer 200 g",        "Soft, fresh cottage cheese made from full-fat milk." },
        { "Surf Excel Easy Wash 1 kg",          "Advanced detergent powder for sparkling clean clothes." },
        { "Haldiram's Aloo Bhujia 400 g",       "Crispy sev made from potato and gram flour, classic Indian snack." },
        { "Britannia Marie Gold Biscuits 250 g","Light, crispy biscuits perfect with tea. Zero trans-fat." },
        { "MTR Poha 500 g",                     "Premium quality flattened rice for quick South Indian breakfast." },
        { "Parachute Coconut Oil 200 ml",       "100% pure coconut oil for cooking and hair care." },
        { "Maggi 2-Minute Noodles — 12 pack",  "Iconic masala flavoured instant noodles, family pack." },
        { "Organic Arhar Dal 500 g",            "Certified organic split pigeon peas, rich in protein and fibre." },
        { "Dettol Original Soap 125 g (3+1)",   "Antibacterial soap with PCMX, provides 12-hour protection." },
        { "Fresh Coriander Bunch 100 g",        "Hand-picked fresh coriander leaves, washed and bunched." },
        { "Kissan Mixed Fruit Jam 500 g",       "Made from real fruit pulp, no artificial colours or flavours." },
    };

    public JioMartAdapter(VendorProperties props) {
        super(props);
        log.info("JioMartAdapter initialised in {} mode", props.isMockMode() ? "MOCK" : "LIVE");
    }

    // ── Identity ──────────────────────────────────────────────────────

    @Override public String getVendorName() { return VENDOR_NAME; }
    @Override public String getLogoUrl()    { return LOGO_URL; }

    @Override
    protected VendorProperties.VendorKey vendorKey() {
        return props.getJiomart();
    }

    // ── Live API wiring ───────────────────────────────────────────────

    @Override
    protected String buildSearchUrl(VendorSearchRequest request) {
        String base = vendorKey().getEndpoint();
        if (base == null || base.isBlank()) base = "https://api.jiomart.com/partner/v1";

        return UriComponentsBuilder.fromHttpUrl(base + "/products/search")
                .queryParam("keyword", request.getQuery() != null ? request.getQuery() : "")
                .queryParam("pincode", request.getLocation() != null ? request.getLocation() : "")
                .queryParam("limit",   request.getMaxResults() != null ? request.getMaxResults() : 10)
                .toUriString();
    }

    @Override
    protected HttpHeaders buildSearchHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vendorKey().getApiKey());     // Authorization: Bearer <key>
        headers.set("X-Merchant-Id", vendorKey().getMerchantId());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Parse JioMart API response (nested structure):
     * { "data": { "items": [ { "productName", "shortDesc", "offerPrice",
     *                           "deliverySlot", "avgRating", "imageUrl" } ] } }
     *
     * Uses only standard Map<String,Object> — no custom libraries.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<VendorSearchResult> parseSearchResponse(Map<String, Object> body) {
        List<VendorSearchResult> results = new ArrayList<>();

        // Navigate: body → data → items
        Object dataRaw = body.get("data");
        if (!(dataRaw instanceof Map)) {
            log.warn("JioMart response missing 'data' object");
            return results;
        }
        Map<String, Object> data = (Map<String, Object>) dataRaw;

        Object itemsRaw = data.get("items");
        if (!(itemsRaw instanceof List)) {
            log.warn("JioMart response missing 'data.items' array");
            return results;
        }

        for (Object item : (List<?>) itemsRaw) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> p = (Map<String, Object>) item;

            VendorSearchResult r = new VendorSearchResult();
            r.setVendorName(VENDOR_NAME);
            r.setItemName(stringOf(p, "productName"));
            r.setDescription(stringOf(p, "shortDesc"));

            // Use offerPrice first, fall back to mrp
            double price = doubleOf(p, "offerPrice");
            if (price == 0) price = doubleOf(p, "mrp");
            r.setPrice(BigDecimal.valueOf(price));
            r.setCurrency("INR");

            // JioMart uses slot strings like "Tomorrow 9 AM–12 PM", not eta minutes
            String slot = stringOf(p, "deliverySlot");
            r.setEtaMinutes(slot.isBlank() ? 1440 : 1440);  // treat as next-day
            r.setEtaLabel(slot.isBlank() ? "Next day delivery" : slot);

            r.setRating(doubleOf(p, "avgRating"));
            r.setImageUrl(stringOf(p, "imageUrl"));
            r.setAvailable(true);

            results.add(r);
        }
        return results;
    }

    @Override
    protected String[][] mockItems() { return MOCK_ITEMS; }

    // ── Parsing helpers ───────────────────────────────────────────────

    private String stringOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    private double doubleOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0.0;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
