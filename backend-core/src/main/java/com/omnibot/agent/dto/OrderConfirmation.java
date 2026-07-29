package com.omnibot.agent.dto;

public class OrderConfirmation {
    private String orderId;
    private String vendorId;
    private String vendorName;
    private String status;
    private String items;
    private String deliveryAddress;
    private String estimatedDelivery;
    private String totalAmount;

    public OrderConfirmation() {}

    public OrderConfirmation(String orderId, String vendorId, String vendorName, String status, String items, String deliveryAddress, String estimatedDelivery, String totalAmount) {
        this.orderId = orderId;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.estimatedDelivery = estimatedDelivery;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
}
