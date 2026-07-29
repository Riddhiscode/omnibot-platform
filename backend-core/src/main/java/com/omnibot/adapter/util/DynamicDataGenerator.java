package com.omnibot.adapter.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

public class DynamicDataGenerator {

    /**
     * Calculates surge pricing multiplier based on time of day.
     * Surge happens during morning rush (8am-10am) and evening rush (6pm-9pm).
     */
    public static BigDecimal getSurgeMultiplier() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        
        if ((hour >= 8 && hour <= 10) || (hour >= 18 && hour <= 21)) {
            return BigDecimal.valueOf(1.5 + (ThreadLocalRandom.current().nextDouble() * 0.5)); // 1.5x to 2.0x
        } else if (hour >= 23 || hour <= 4) {
            return BigDecimal.valueOf(1.2 + (ThreadLocalRandom.current().nextDouble() * 0.3)); // 1.2x to 1.5x for late night
        }
        return BigDecimal.ONE; // Normal hours
    }

    /**
     * Applies surge multiplier to a base price.
     */
    public static BigDecimal applySurge(BigDecimal basePrice) {
        return basePrice.multiply(getSurgeMultiplier()).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Calculates realistic dynamic ETA based on time of day and traffic.
     * Returns higher ETA during rush hours.
     */
    public static int calculateDynamicEta(int baseMin, int baseMax) {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int eta = baseMin + ThreadLocalRandom.current().nextInt(baseMax - baseMin);
        
        if ((hour >= 8 && hour <= 10) || (hour >= 18 && hour <= 21)) {
            eta += 15; // +15 mins traffic delay
        } else if (hour >= 13 && hour <= 14) {
            eta += 10; // lunch rush
        } else if (hour >= 23 || hour <= 4) {
            eta -= 5; // Fast delivery late night
        }
        return Math.max(5, eta); // Minimum 5 mins
    }
}
