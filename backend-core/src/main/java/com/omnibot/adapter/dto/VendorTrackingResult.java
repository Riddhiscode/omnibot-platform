package com.omnibot.adapter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified tracking result from any vendor.
 */
public class VendorTrackingResult {

    private String externalOrderId;
    private String vendorName;
    private TrackingStatus status;
    private String statusMessage;
    private int etaMinutes;
    private String etaLabel;
    private BigDecimal currentFare;
    private String currency;
    private Double driverLatitude;
    private Double driverLongitude;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;
    private List<TrackingEvent> events;

    public enum TrackingStatus {
        SEARCHING,
        DRIVER_ASSIGNED,
        DRIVER_EN_ROUTE,
        ARRIVED,
        IN_PROGRESS,
        PREPARING,
        OUT_FOR_DELIVERY,
        DELIVERED,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    public VendorTrackingResult() {}

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public TrackingStatus getStatus() { return status; }
    public void setStatus(TrackingStatus status) { this.status = status; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }
    public String getEtaLabel() { return etaLabel; }
    public void setEtaLabel(String etaLabel) { this.etaLabel = etaLabel; }
    public BigDecimal getCurrentFare() { return currentFare; }
    public void setCurrentFare(BigDecimal currentFare) { this.currentFare = currentFare; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Double getDriverLatitude() { return driverLatitude; }
    public void setDriverLatitude(Double driverLatitude) { this.driverLatitude = driverLatitude; }
    public Double getDriverLongitude() { return driverLongitude; }
    public void setDriverLongitude(Double driverLongitude) { this.driverLongitude = driverLongitude; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
    public List<TrackingEvent> getEvents() { return events; }
    public void setEvents(List<TrackingEvent> events) { this.events = events; }

    public static class TrackingEvent {
        private LocalDateTime timestamp;
        private String status;
        private String description;

        public TrackingEvent() {}
        public TrackingEvent(LocalDateTime timestamp, String status, String description) {
            this.timestamp = timestamp;
            this.status = status;
            this.description = description;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
