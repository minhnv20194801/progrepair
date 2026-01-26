package org.group10.cli;

import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.program.Program;
import org.group10.suspiciouscalculator.TarantulaSuspiciousCalculator;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "test",
        description = "Repair a buggy program using genetic search"
)
public class TestCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-d", "--dir"}, required = true)
    private String dirPath;

    @CommandLine.Option(names = {"-c", "--class"}, required = true)
    private String classname;

    @Override
    public Integer call() {
        try {
            Program program = new Program(dirPath, classname, new ClassicGenProgMutator(), new RawProgramCrossover(), new TarantulaSuspiciousCalculator(), new WeightedFitnessFunction(1.0, 1.0));
            program.executeTestSuiteWithLog();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }
}
