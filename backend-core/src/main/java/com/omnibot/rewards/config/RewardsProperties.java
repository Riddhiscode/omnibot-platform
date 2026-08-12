package com.omnibot.rewards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "omnibot.rewards")
public class RewardsProperties {

    /** Rupees spent to earn one base point (before tier multiplier). */
    private int rupeesPerPoint = 20;

    /** Rupee value of each point when redeemed. */
    private double redeemRateRupees = 0.10;

    /** Minimum points required for a redemption. */
    private int minRedemptionPoints = 100;

    public int getRupeesPerPoint() { return rupeesPerPoint; }
    public void setRupeesPerPoint(int rupeesPerPoint) { this.rupeesPerPoint = rupeesPerPoint; }

    public double getRedeemRateRupees() { return redeemRateRupees; }
    public void setRedeemRateRupees(double redeemRateRupees) { this.redeemRateRupees = redeemRateRupees; }

    public int getMinRedemptionPoints() { return minRedemptionPoints; }
    public void setMinRedemptionPoints(int minRedemptionPoints) { this.minRedemptionPoints = minRedemptionPoints; }
}
