package com.omnibot.adapter.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * A single search result from any vendor.
 * Unified schema that maps to the frontend ServiceCard.
 */
public class VendorSearchResult {

    private String vendorName;
    private String itemName;
    private String description;
    private BigDecimal price;
    private String currency;
    private int etaMinutes;
    private String etaLabel;
    private double rating;
    private int reviewCount;
    private String imageUrl;
    private boolean isAvailable;
    private boolean isCheapest;
    private boolean isFastest;
    private String promoText;
    private List<String> tags;

    public VendorSearchResult() {}

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }
    public String getEtaLabel() { return etaLabel; }
    public void setEtaLabel(String etaLabel) { this.etaLabel = etaLabel; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public boolean isCheapest() { return isCheapest; }
    public void setCheapest(boolean cheapest) { isCheapest = cheapest; }
    public boolean isFastest() { return isFastest; }
    public void setFastest(boolean fastest) { isFastest = fastest; }
    public String getPromoText() { return promoText; }
    public void setPromoText(String promoText) { this.promoText = promoText; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
