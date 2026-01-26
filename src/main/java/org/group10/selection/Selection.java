package org.group10.selection;

import java.util.List;

/**
 * Interface for a selection strategy for choosing an individual from a population.
 *
 * @param <T> the type of individuals in the population
 */
public interface Selection<T> {
    /**
     * Selects an individual from the given population according to a specific
     * selection strategy.
     *
     * @param population the list of individuals to select from
     * @return the selected individual
     */
    T select(List<T> population);
}
