package org.group10.crossover;

import java.util.Map;

public interface Crossover<T> {
    // using Map.Entry as a pair to be returned
    public abstract Map.Entry<T, T> crossover(T parent1, T parent2);
}
