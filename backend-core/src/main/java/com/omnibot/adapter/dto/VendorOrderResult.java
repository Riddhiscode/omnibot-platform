package com.omnibot.adapter.dto;

import java.math.BigDecimal;

/**
 * Unified order result from any vendor.
 */
public class VendorOrderResult {

    private boolean success;
    private String externalOrderId;
    private String trackingUrl;
    private BigDecimal amountCharged;
    private String currency;
    private String status;
    private String vendorName;
    private String estimatedDelivery;
    private String errorMessage;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;

    public VendorOrderResult() {}

    public static VendorOrderResult success(String vendorName, String externalOrderId,
                                            BigDecimal amount, String trackingUrl) {
        VendorOrderResult r = new VendorOrderResult();
        r.success = true;
        r.vendorName = vendorName;
        r.externalOrderId = externalOrderId;
        r.amountCharged = amount;
        r.currency = "INR";
        r.status = "CONFIRMED";
        r.trackingUrl = trackingUrl;
        return r;
    }

    public static VendorOrderResult failure(String vendorName, String error) {
        VendorOrderResult r = new VendorOrderResult();
        r.success = false;
        r.vendorName = vendorName;
        r.status = "FAILED";
        r.errorMessage = error;
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getTrackingUrl() { return trackingUrl; }
    public void setTrackingUrl(String trackingUrl) { this.trackingUrl = trackingUrl; }
    public BigDecimal getAmountCharged() { return amountCharged; }
    public void setAmountCharged(BigDecimal amountCharged) { this.amountCharged = amountCharged; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
}
