package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.Map;

/**
 * Interface for a strategy for calculating suspiciousness scores for a program's lines.
 * <p>
 * Suspiciousness scores are typically used in fault localization to indicate
 * how likely each line is to be the cause of test failures. The scores are
 * used by mutation or repair algorithms to prioritize modifications.
 * </p>
 */
public interface SuspiciousCalculator {
    /**
     * Calculates a suspiciousness score for each line of the given program.
     *
     * @param targetProgram the program for which to compute suspiciousness scores
     * @return a map where the keys are line numbers and the values
     *         are the corresponding suspiciousness scores
     */
    Map<Integer, Double> calculateScore(Program targetProgram);
}
