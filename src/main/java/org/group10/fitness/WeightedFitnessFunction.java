package org.group10.fitness;

import org.group10.program.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * A fitness function that evaluates {@link Program} instances using a
 * weighted combination of positive and negative test outcomes. <br>
 *
 * Positive tests indicate the tests that were successful in the original program. <br>
 * Negative tests indicate the tests that were failed in the original program. <br>
 *
 * NOTE: the first program that gets evaluated by the fitness function will be considered
 * the original program. All the subsequent programs that get evaluated will be based on this
 * original program. <br>
 *
 * If a non-compilable program get evaluated, its fitness score will be 0.
 */
public class WeightedFitnessFunction implements FitnessFunction<Program> {
    /**
     * Weight applied to positive test results.
     */
    private final double positiveTestWeight;

    /**
     * Weight applied to negative test results.
     */
    private final double negativeTestWeight;

    /**
     * The original program used as a baseline for fitness evaluation.
     */
    private Program originalProgram;

    /**
     * Creates an instance of {@link WeightedFitnessFunction} with the given test weights.
     *
     * @param positiveTestWeight weight applied to positive tests
     * @param negativeTestWeight weight applied negative tests
     */
    public WeightedFitnessFunction(double positiveTestWeight, double negativeTestWeight) {
        this.positiveTestWeight = positiveTestWeight;
        this.negativeTestWeight = negativeTestWeight;
    }

    /**
     * Calculates the fitness of the given program. <br>
     *
     * The fitness value is computed as: <br>
     * {@code (successful tests that were also successful in the original program (positive tests) × positive weight)
     * + (successful tests that were also failed in the original program (negative tests) × negative weight)} <br>
     *
     * NOTE: The first evaluated program is stored as the original program
     * and used for all subsequent comparisons. <br>
     *
     * @param target the program to evaluate
     * @return the calculated fitness value, or {@code 0} if evaluation fails
     */
    @Override
    public double calculateFitness(Program target) {
        if (target.isNotCompilable()) {
            return 0;
        }

        try {
            target.executeTestSuite();
        } catch (Exception e) {
            return 0;
        }

        if (originalProgram == null) {
            originalProgram = target;
        }

        List<String> tmpList = new ArrayList<>(target.getPositiveTests());
        tmpList.retainAll(originalProgram.getPositiveTests());
        long positiveCount = tmpList.size();

        tmpList = new ArrayList<>(target.getPositiveTests());
        tmpList.retainAll(originalProgram.getNegativeTests());
        long negativeCount = tmpList.size();

        return positiveCount * positiveTestWeight + negativeCount * negativeTestWeight;
    }

    /**
     * Determines whether the given program has achieved the maximum possible
     * fitness. <br>
     *
     * A program is considered maximal if it successfully passes all tests.
     *
     * @param target the program being evaluated
     * @return {@code true} if the program has achieved maximum fitness;
     *         {@code false} otherwise
     */
    @Override
    public boolean isAtMaxValue(Program target) {
        try {
            target.executeTestSuite();
        } catch (Exception e) {
            return false;
        }

        return (target.getTestSuccessfulCount() == (originalProgram.getTestSuccessfulCount() + originalProgram.getTestFailedCount()));
    }

    @Override
    public String toString() {
        return "WeightedFitnessFuction with postive_weight=" + positiveTestWeight + ", negative_weight=" + negativeTestWeight;
    }
}
