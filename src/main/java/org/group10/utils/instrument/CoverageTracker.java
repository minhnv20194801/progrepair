package org.group10.utils.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks execution of lines in instrumented classes for code coverage purposes. <br>
 *
 * This class is used in conjunction with {@link CoverageInstrumenter}. Each time
 * a line in an instrumented class is executed, {@link #hit(int)} is called with
 * the line number, recording that it was executed.
 */
public class CoverageTracker {
    /**
     * Maps line numbers to the number of times they have been executed.
     */
    private static final Map<Integer, Integer> lineHits = new HashMap<>();

    /**
     * Records that the specified line has been executed.
     *
     * @param line the line number that was executed
     */
    public static void hit(int line) {
        lineHits.put(line, lineHits.getOrDefault(line, 0) + 1);
    }

    /**
     * Resets all recorded line execution data.
     */
    public static void reset() {
        lineHits.clear();
    }

    /**
     * Returns a list of line numbers that have been executed at least once.
     *
     * @return a list of executed line numbers
     */
    public static List<Integer> getExecutedLines() {
        return new ArrayList<>(lineHits.keySet());
    }
}
