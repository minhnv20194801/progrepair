package org.group10.fitness;

import org.group10.program.Program;

import java.util.ArrayList;
import java.util.List;

public class WeightedFitnessFunction implements FitnessFunction<Program> {
    private final double positiveTestWeight;
    private final double negativeTestWeight;
    private Program originalProgram;

    public WeightedFitnessFunction(double positiveTestWeight, double negativeTestWeight) {
        this.positiveTestWeight = positiveTestWeight;
        this.negativeTestWeight = negativeTestWeight;
    }

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

    @Override
    public boolean isMax(Program target) {
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
