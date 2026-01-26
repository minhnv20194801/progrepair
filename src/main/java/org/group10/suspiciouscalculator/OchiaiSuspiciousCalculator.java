package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Ochiai fault localization technique for calculating
 * suspiciousness scores of program statements. <br>
 *
 * The formula is: <br>
 * <pre>
 *     suspiciousness = ef / sqrt((ef + nf) * (ef + ep))
 * </pre>
 * where:
 * <ul>
 *     <li>ef = number of failing tests that execute the statement</li>
 *     <li>nf = number of failing tests that do not execute the statement</li>
 *     <li>ep = number of passing tests that execute the statement</li>
 * </ul>
 * </p>
 * Statements not executed by any failing test are ignored (suspiciousness = 0).
 */
public class OchiaiSuspiciousCalculator implements SuspiciousCalculator {
    /**
     * Calculates the suspiciousness scores for each line of the target program
     * using the Ochiai metric.
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

        Map<Integer, Integer> efs = targetProgram.getEfs();
        Map<Integer, Integer> eps = targetProgram.getEps();
        Map<Integer, Integer> nfs = targetProgram.getNfs();

        for (int stmtIndex = 0; stmtIndex < targetProgram.getSize(); stmtIndex++) {
            int ef = efs.getOrDefault(stmtIndex, 0);
            if (ef == 0) continue;
            int ep = eps.getOrDefault(stmtIndex, 0);
            int nf = nfs.getOrDefault(stmtIndex, 0);

            double score;

            score = ef /
                    Math.sqrt((double) (ef + nf) * (ef + ep));
            suspiciousScores.put(stmtIndex, score);
        }

        return suspiciousScores;
    }

    @Override
    public String toString() {
        return "OchiaiFaultLocalization";
    }
}
