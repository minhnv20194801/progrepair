package org.group10.utils.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverageTracker {
    private static final Map<Integer, Integer> lineHits = new HashMap<>();

    public static void hit(int line) {
        lineHits.put(line, lineHits.getOrDefault(line, 0) + 1);
    }

    public static void reset() {
        lineHits.clear();
    }

    public static List<Integer> getExecutedLines() {
        return new ArrayList<>(lineHits.keySet());
    }
}
