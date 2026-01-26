package org.group10.utils;

import java.util.*;

/**
 * Utility class for randomness-related operations. <br>
 *
 * Provides a shared {@link Random} instance and methods for generating
 * random values with specific behavior.
 */
public class Randomness {
    private static final Random RANDOM = new Random();

    public static Random getRandom() {
        return RANDOM;
    }

    public static int getRandomIntegerWithWeighted(Map<Integer, Double> weightsMap) {
        double totalWeight = weightsMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalWeight == 0.0) {
            return 0;
        }

        double r = RANDOM.nextDouble(totalWeight);
        double cumulative = 0.0;

        List<Map.Entry<Integer, Double>> entries =
                new ArrayList<>(weightsMap.entrySet());

        Collections.shuffle(entries);

        Map<Integer, Double> shuffled = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> e : entries) {
            shuffled.put(e.getKey(), e.getValue());
        }

        for (Map.Entry<Integer, Double> e : shuffled.entrySet()) {
            cumulative += e.getValue();
            if (r < cumulative) {
                return e.getKey();
            }
        }

        return weightsMap.keySet().iterator().next();
    }
}
