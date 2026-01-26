package org.group10.mutator;

/**
 * Interface for a mutation operator used in genetic algorithms. <br>
 *
 * A mutator produces a modified version of a given individual by applying
 * some form of mutation.
 *
 * @param <T> the type of individuals this mutator operates on
 */
public interface Mutator<T> {
    /**
     * Applies a mutation to the given target.
     *
     * @param target the individual to mutate
     * @return a mutated version of the target
     */
    T mutate(T target);
}
