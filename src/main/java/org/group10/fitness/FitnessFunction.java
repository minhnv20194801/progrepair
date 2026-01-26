package org.group10.fitness;

/**
 * Interface for a fitness function used to evaluate individuals in a
 * genetic algorithm.
 *
 * @param <T> the type of individuals evaluated by this fitness function
 */
public interface FitnessFunction<T> {
    /**
     * Calculates the fitness value of the given target.
     *
     * @param target the individual to evaluate
     * @return the fitness score for the target
     */
    double calculateFitness(T target);

    /**
     * Indicates whether the fitness of the given target has achieved the
     * maximum possible value.
     *
     * @param target the individual being evaluated
     * @return {@code true} if the fitness is at the maximum possible value;
     * {@code false} if it is otherwise
     */
    boolean isAtMaxValue(T target);
}
