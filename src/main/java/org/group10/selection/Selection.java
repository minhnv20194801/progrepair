package org.group10.selection;

import java.util.List;

public interface Selection<T> {
    public T select(List<T> population);
}
