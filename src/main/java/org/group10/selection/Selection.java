package org.group10.selection;

import java.util.List;

public interface Selection<T> {
    T select(List<T> population);
}
