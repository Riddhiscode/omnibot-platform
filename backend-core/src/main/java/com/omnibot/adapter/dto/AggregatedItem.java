package com.omnibot.adapter.dto;

public class AggregatedItem {
    private String provider;
    private String category;
    private String eta;
    private String priceOrDelivery;
    private Double rating;
    private String deepLinkUrl;
    private String actionType; // "DIRECT_API" | "DEEP_LINK"
    private boolean isCheapest;
    private boolean isFastest;
    private String logo;

    public AggregatedItem() {}

    public AggregatedItem(String provider, String category, String eta, String priceOrDelivery, Double rating, String deepLinkUrl, String actionType, String logo) {
        this.provider = provider;
        this.category = category;
        this.eta = eta;
        this.priceOrDelivery = priceOrDelivery;
        this.rating = rating;
        this.deepLinkUrl = deepLinkUrl;
        this.actionType = actionType;
        this.logo = logo;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getPriceOrDelivery() { return priceOrDelivery; }
    public void setPriceOrDelivery(String priceOrDelivery) { this.priceOrDelivery = priceOrDelivery; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getDeepLinkUrl() { return deepLinkUrl; }
    public void setDeepLinkUrl(String deepLinkUrl) { this.deepLinkUrl = deepLinkUrl; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public boolean isCheapest() { return isCheapest; }
    public void setCheapest(boolean cheapest) { isCheapest = cheapest; }

    public boolean isFastest() { return isFastest; }
    public void setFastest(boolean fastest) { isFastest = fastest; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}
