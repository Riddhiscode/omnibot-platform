package com.omnibot.adapter.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DeepLinkGenerator {

    public static String generateDeepLink(String provider, String category, String query, double lat, double lng) {
        String encodedQuery = query != null ? URLEncoder.encode(query, StandardCharsets.UTF_8) : "";
        String p = provider != null ? provider.toLowerCase().trim() : "";

        switch (p) {
            case "zomato":
                return "zomato://search?q=" + encodedQuery;
            case "swiggy":
                return "swiggy://explore?query=" + encodedQuery;
            case "uber":
                return "uber://?action=setPickup&pickup=my_location&dropoff[latitude]=" + lat + "&dropoff[longitude]=" + lng;
            case "ola":
                return "olacabs://app/launch?lat=" + lat + "&lng=" + lng;
            case "rapido":
                return "rapido://booking?lat=" + lat + "&lng=" + lng;
            case "amazon":
            case "amazon fresh":
            case "amazon now":
                return "https://www.amazon.in/s?k=" + encodedQuery + "&i=nowstore";
            case "flipkart":
            case "flipkart minutes":
                return "https://www.flipkart.com/search?q=" + encodedQuery;
            case "nykaa":
                return "https://www.nykaa.com/search/result/?q=" + encodedQuery;
            case "blinkit":
                return "https://blinkit.com/s/?q=" + encodedQuery;
            case "zepto":
                return "https://www.zepto.co.in/search?q=" + encodedQuery;
            case "bigbasket":
                return "https://www.bigbasket.com/ps/?q=" + encodedQuery;
            case "jiomart":
                return "https://www.jiomart.com/search/" + encodedQuery;
            default:
                return "https://www.google.com/search?q=" + encodedQuery + "+" + provider;
        }
    }

    public static String resolveActionType(String provider) {
        String p = provider != null ? provider.toLowerCase().trim() : "";
        // Layer A (Direct API enabled platforms) vs Layer B (Deep Link Fallback)
        if (p.equals("zomato") || p.equals("swiggy") || p.equals("uber") || p.equals("ola") || p.equals("bigbasket") || p.equals("amazon")) {
            return "DIRECT_API";
        }
        return "DEEP_LINK";
    }
}
