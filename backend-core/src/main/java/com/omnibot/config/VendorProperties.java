package com.omnibot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Central configuration for all vendor integrations.
 *
 * Mode toggle:
 *   omnibot.vendor.mode=mock   -> All adapters return simulated data (default)
 *   omnibot.vendor.mode=live   -> Adapters make real HTTP calls (requires API keys)
 *
 * API keys are loaded from environment variables or application.properties.
 * Example: omnibot.zomato.api.key=${ZOMATO_API_KEY:}
 */
@Configuration
@ConfigurationProperties(prefix = "omnibot.vendor")
public class VendorProperties {

    /** "mock" or "live" — master switch for all vendor adapters. */
    private String mode = "mock";

    /** Global timeout for vendor API calls in milliseconds. */
    private int timeoutMs = 5000;

    /** Number of retry attempts for failed vendor calls. */
    private int maxRetries = 2;

    // Food vendor keys
    private VendorKey zomato = new VendorKey();
    private VendorKey swiggy = new VendorKey();
    private VendorKey ubereats = new VendorKey();
    private VendorKey doordash = new VendorKey();

    // Transport vendor keys
    private VendorKey uber = new VendorKey();
    private VendorKey ola = new VendorKey();
    private VendorKey lyft = new VendorKey();
    private VendorKey bolt = new VendorKey();
    private VendorKey rapido = new VendorKey();
    private VendorKey yulu = new VendorKey();

    // Shopping vendor keys
    private VendorKey amazon = new VendorKey();
    private VendorKey flipkart = new VendorKey();
    private VendorKey meesho = new VendorKey();
    private VendorKey myntra = new VendorKey();
    private VendorKey ebay = new VendorKey();

    // Grocery vendor keys (Phase 3)
    private VendorKey bigbasket = new VendorKey();
    private VendorKey jiomart  = new VendorKey();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public boolean isLiveMode() { return "live".equalsIgnoreCase(mode); }
    public boolean isMockMode() { return !isLiveMode(); }

    public VendorKey getZomato() { return zomato; }
    public void setZomato(VendorKey zomato) { this.zomato = zomato; }
    public VendorKey getSwiggy() { return swiggy; }
    public void setSwiggy(VendorKey swiggy) { this.swiggy = swiggy; }
    public VendorKey getUbereats() { return ubereats; }
    public void setUbereats(VendorKey ubereats) { this.ubereats = ubereats; }
    public VendorKey getDoordash() { return doordash; }
    public void setDoordash(VendorKey doordash) { this.doordash = doordash; }
    public VendorKey getUber() { return uber; }
    public void setUber(VendorKey uber) { this.uber = uber; }
    public VendorKey getOla() { return ola; }
    public void setOla(VendorKey ola) { this.ola = ola; }
    public VendorKey getLyft() { return lyft; }
    public void setLyft(VendorKey lyft) { this.lyft = lyft; }
    public VendorKey getBolt() { return bolt; }
    public void setBolt(VendorKey bolt) { this.bolt = bolt; }
    public VendorKey getRapido() { return rapido; }
    public void setRapido(VendorKey rapido) { this.rapido = rapido; }
    public VendorKey getYulu() { return yulu; }
    public void setYulu(VendorKey yulu) { this.yulu = yulu; }
    public VendorKey getAmazon() { return amazon; }
    public void setAmazon(VendorKey amazon) { this.amazon = amazon; }
    public VendorKey getFlipkart() { return flipkart; }
    public void setFlipkart(VendorKey flipkart) { this.flipkart = flipkart; }
    public VendorKey getMeesho() { return meesho; }
    public void setMeesho(VendorKey meesho) { this.meesho = meesho; }
    public VendorKey getMyntra() { return myntra; }
    public void setMyntra(VendorKey myntra) { this.myntra = myntra; }
    public VendorKey getEbay() { return ebay; }
    public void setEbay(VendorKey ebay) { this.ebay = ebay; }

    // Grocery (Phase 3)
    public VendorKey getBigbasket() { return bigbasket; }
    public void setBigbasket(VendorKey bigbasket) { this.bigbasket = bigbasket; }
    public VendorKey getJiomart() { return jiomart; }
    public void setJiomart(VendorKey jiomart) { this.jiomart = jiomart; }

    /**
     * API key + endpoint config for a single vendor.
     */
    public static class VendorKey {
        private String apiKey = "";
        private String apiSecret = "";
        private String endpoint = "";
        private String merchantId = "";
        private boolean enabled = true;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean hasApiKey() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
