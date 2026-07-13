package com.omnibot.adapter.dto;

import java.util.Map;

/**
 * Unified search request sent to any vendor adapter.
 * Fields are interpreted differently per category:
 *   Food:     query = cuisine/dish, location = delivery address
 *   Transport: query = ride type,   location = pickup coordinates
 *   Shopping:  query = product name, location = delivery pincode
 */
public class VendorSearchRequest {

    private String query;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer maxResults;
    private Double maxPrice;
    private Double minRating;
    private Map<String, String> filters;

    public VendorSearchRequest() { this.maxResults = 10; }

    public VendorSearchRequest(String query, String location) {
        this.query = query;
        this.location = location;
        this.maxResults = 10;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }
    public Map<String, String> getFilters() { return filters; }
    public void setFilters(Map<String, String> filters) { this.filters = filters; }
}
