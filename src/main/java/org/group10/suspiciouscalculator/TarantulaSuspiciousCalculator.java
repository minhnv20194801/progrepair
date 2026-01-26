package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Tarantula fault localization technique for calculating
 * suspiciousness scores of program statements. <br>
 *
 * The formula is:
 * <pre>
 *     suspiciousness = (ef / totalFailed) / ((ef / totalFailed) + (ep / totalPassed))
 * </pre>
 * where:
 * <ul>
 *     <li>ef = number of failing tests that execute the statement</li>
 *     <li>ep = number of passing tests that execute the statement</li>
 *     <li>totalFailed = total number of failing tests</li>
 *     <li>totalPassed = total number of passing tests</li>
 * </ul>
 * Statements not executed by any failing test are ignored (suspiciousness = 0).
 */
public class TarantulaSuspiciousCalculator implements SuspiciousCalculator {
    /**
     * Calculates the suspiciousness scores for each line of the target program
     * using the Tarantula metric.
     *
     * @param targetProgram the program whose lines are to be scored
     * @return a map where the key is the 0-based line index and the value is
     *         the calculated suspiciousness score. Lines with no failing test executions
     *         are omitted.
     */
    @Override
    public Map<Integer, Double> calculateScore(Program targetProgram) {
        if (targetProgram.isNotCompilable()) {
            return new HashMap<>();
        }

        try {
            targetProgram.executeTestSuite();
        } catch (Exception e) {
            return new HashMap<>();
        }

        Map<Integer, Double> suspiciousScores = new HashMap<>();

        long testFailedCount = targetProgram.getTestFailedCount();
        long testSucceededCount = targetProgram.getTestSuccessfulCount();

        Map<Integer, Integer> efs = targetProgram.getEfs();
        Map<Integer, Integer> eps = targetProgram.getEps();

        for (int stmtIndex = 0; stmtIndex < targetProgram.getSize(); stmtIndex++) {
            int ef = efs.getOrDefault(stmtIndex, 0);
            if (ef == 0) continue;
            int ep = eps.getOrDefault(stmtIndex, 0);

            double score;
            double failedRatio = (double) ef / testFailedCount;
            double passedRatio = (double) ep / testSucceededCount;

            score = failedRatio / (failedRatio + passedRatio);
            suspiciousScores.put(stmtIndex, score);
        }

        return suspiciousScores;
    }

    @Override
    public String toString() {
        return "TarantulaFaultLocalization";
    }
}
