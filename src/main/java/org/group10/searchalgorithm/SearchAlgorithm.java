package org.group10.searchalgorithm;

/**
 * Interface for a generic search algorithm that explores a search space to find a solution. <br>
 *
 * @param <T> the type of elements in the search space
 */
public interface SearchAlgorithm<T> {
    /**
     * Performs the search starting from the given initial point. <br>
     *
     * @param startPoint the starting point of the search
     * @return the result of the search after reaching stopping condition. <br>
     * Stopping condition can either be that the optimal point reached, or search
     * resources exhausted.
     */
    T search(T startPoint);
}
