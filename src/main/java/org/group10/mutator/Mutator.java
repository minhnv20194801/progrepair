package org.group10.mutator;

public interface Mutator<T> {
    public abstract T mutate(T target);
}
