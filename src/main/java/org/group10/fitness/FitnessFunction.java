package org.group10.fitness;

public interface FitnessFunction<T> {
    double calculateFitness(T target);

    boolean isMax(T target);
}
