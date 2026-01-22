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
            return 0;
        }

        List<String> tmpList = new ArrayList<>(target.getPositiveTests());
        tmpList.retainAll(originalProgram.getPositiveTests());
        long positiveCount = tmpList.size();

        tmpList = new ArrayList<>(target.getPositiveTests());
        tmpList.retainAll(originalProgram.getNegativeTests());
        long negativeCount = tmpList.size();

        return positiveCount*positiveTestWeight + negativeCount*negativeTestWeight;
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

    public void setOriginalProgram(Program originalProgram) {
        if (this.originalProgram == null) {
            try {
                originalProgram.executeTestSuite();
            } catch (Exception e) {
                return;
            }

            this.originalProgram = originalProgram;
        }
    }
}
