package org.group10.fitness;

public interface FitnessFunction<T> {
    public abstract double calculateFitness(T target);

    public boolean isMax(T target);
}
