package com.omnibot.adapter;

import com.omnibot.adapter.dto.*;

import java.util.List;

/**
 * Universal contract every vendor adapter must implement.
 *
 * Each vendor (Zomato, Uber, Amazon, etc.) provides its own implementation.
 * Adapters contain BOTH mock and live logic, toggled by omnibot.vendor.mode.
 *
 * When mode=mock: returns realistic randomized data for development.
 * When mode=live: makes real HTTP calls to vendor APIs (TODO: fill in endpoints).
 */
public interface VendorAdapter {

    /** Human-readable vendor name (e.g. "Zomato", "Uber", "Amazon"). */
    String getVendorName();

    /** Vendor logo URL for UI rendering. */
    String getLogoUrl();

    /** Which category this vendor belongs to. */
    VendorCategory getCategory();

    /** The service class/action this vendor provides (e.g. "ORDER_FOOD", "BOOK_RIDE", "BUY_NOW"). */
    String getServiceAction();

    /** Whether this vendor adapter is currently available (API key present, service healthy). */
    boolean isAvailable();

    /**
     * Search for offerings from this vendor.
     * For food: menu items / restaurants.
     * For transport: available ride types.
     * For shopping: product listings.
     */
    List<VendorSearchResult> search(VendorSearchRequest request);

    /**
     * Place an order/booking with this vendor.
     * Returns order confirmation with external tracking info.
     */
    VendorOrderResult placeOrder(VendorOrderRequest request);

    /**
     * Track an existing order by its external order ID.
     */
    VendorTrackingResult trackOrder(String externalOrderId);
}
