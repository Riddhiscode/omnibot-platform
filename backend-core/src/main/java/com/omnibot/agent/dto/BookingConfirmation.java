package com.omnibot.agent.dto;

public class BookingConfirmation {
    private String bookingId;
    private String vendorId;
    private String vendorName;
    private String status;
    private String pickup;
    private String destination;
    private String eta;
    private String price;

    public BookingConfirmation() {}

    public BookingConfirmation(String bookingId, String vendorId, String vendorName, String status, String pickup, String destination, String eta, String price) {
        this.bookingId = bookingId;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.status = status;
        this.pickup = pickup;
        this.destination = destination;
        this.eta = eta;
        this.price = price;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}
