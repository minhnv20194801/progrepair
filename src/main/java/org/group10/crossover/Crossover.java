package org.group10.crossover;

import java.util.Map;

/**
 * Interface for a crossover operator used in genetic algorithms,
 * which combines two parent individuals to produce two offspring
 *
 * @param <T> the type of individuals this crossover operates on
 */
public interface Crossover<T> {
    /**
     * Performs a crossover operation on two parent individuals. <br>
     *
     * The returned {@link Map.Entry} is used as a simple pair, where:
     * <ul>
     *   <li>the key represents the first offspring</li>
     *   <li>the value represents the second offspring</li>
     * </ul>
     *
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return a {@link Map.Entry} act as a pair containing the two
     * offspring produced by the crossover
     */
    Map.Entry<T, T> crossover(T parent1, T parent2);
}
