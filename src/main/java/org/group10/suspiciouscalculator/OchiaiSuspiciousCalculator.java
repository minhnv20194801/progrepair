package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.HashMap;
import java.util.Map;

public class OchiaiSuspiciousCalculator implements SuspiciousCalculator {
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
}
