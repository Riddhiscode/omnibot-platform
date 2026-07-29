package com.omnibot.agent.dto;

public class OrderStatus {
    private String orderId;
    private String vendorName;
    private String status;
    private String estimatedArrival;
    private String currentStep;

    public OrderStatus() {}

    public OrderStatus(String orderId, String vendorName, String status, String estimatedArrival, String currentStep) {
        this.orderId = orderId;
        this.vendorName = vendorName;
        this.status = status;
        this.estimatedArrival = estimatedArrival;
        this.currentStep = currentStep;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
}
