package com.omnibot.agent.dto;

public class VendorQuote {
    private String vendorId;
    private String vendorName;
    private String category;
    private String itemName;
    private String price;
    private String eta;
    private String rating;
    private boolean available;

    public VendorQuote() {}

    public VendorQuote(String vendorId, String vendorName, String category, String itemName, String price, String eta, String rating, boolean available) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.category = category;
        this.itemName = itemName;
        this.price = price;
        this.eta = eta;
        this.rating = rating;
        this.available = available;
    }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
