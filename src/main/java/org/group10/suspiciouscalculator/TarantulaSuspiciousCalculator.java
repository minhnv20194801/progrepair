package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.HashMap;
import java.util.Map;

public class TarantulaSuspiciousCalculator implements SuspiciousCalculator {
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
}
