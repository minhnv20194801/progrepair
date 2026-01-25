package org.group10.cli;

import org.group10.crossover.Crossover;
import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.FitnessFunction;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.mutator.Mutator;
import org.group10.program.Program;
import org.group10.searchalgorithm.ClassicGenProgAlgorithm;
import org.group10.searchalgorithm.SearchAlgorithm;
import org.group10.selection.ProgramBinaryTournamentSlection;
import org.group10.selection.Selection;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.suspiciouscalculator.TarantulaSuspiciousCalculator;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "repair",
        description = "Repair a buggy program using genetic search"
)
public class RepairCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d", "--dir"}, required = true)
    private String dirPath;

    @CommandLine.Option(names = {"-c", "--class"}, required = true)
    private String classname;

    @CommandLine.Option(names = "--pos_weight", defaultValue = "1")
    private double positiveWeight;

    @CommandLine.Option(names = "--neg_weight", defaultValue = "10")
    private double negativeWeight;

    @CommandLine.Option(names = {"-m", "--mutation"}, defaultValue = "0.06")
    private double mutationWeight;

    @CommandLine.Option(names = {"-p", "--population"}, defaultValue = "40")
    private int populationSize;

    @CommandLine.Option(names = {"-g", "--generation"}, defaultValue = "200")
    private int maxGeneration;

    @CommandLine.Option(names = {"-fl", "--fault_localization"}, defaultValue = "ochiai")
    private String faultLocalization;

    @CommandLine.Option(names = {"--multiclassmutation"}, defaultValue = "false")
    private boolean canGetFixFromDifferentClasses;

    @CommandLine.Option(names = {"-out", "--output_dir"})
    private String outputDir;

    private Mutator<Program> setupClassicMutator(boolean canGetFixFromDifferentClasses) {
        ClassicGenProgMutator mutator = new ClassicGenProgMutator();
        mutator.setCanGetFixFromDifferentClass(canGetFixFromDifferentClasses);
        return mutator;
    }

    private Crossover<Program> setupRawProgramCrossover() {
        return new RawProgramCrossover();
    }

    private SuspiciousCalculator setupTarantulaSuspiciousCalculator() {
        return new TarantulaSuspiciousCalculator();
    }

    private SuspiciousCalculator setupOchiaiSuspiciousCalculator() {
        return new OchiaiSuspiciousCalculator();
    }

    private FitnessFunction<Program> setupWeightedFitnessFunction(double positiveWeight, double negativeWeight) {
        return new WeightedFitnessFunction(positiveWeight, negativeWeight);
    }

    private Program setupInitialProgram(String dirPath, String className, Mutator<Program> mutator, Crossover<Program> crossover, SuspiciousCalculator suspiciousCalculator, FitnessFunction<Program> fitnessFunction) {
        try {
            Program program = new Program(dirPath, className, mutator, crossover , suspiciousCalculator, fitnessFunction);
            program.executeTestSuiteWithLog();
            program.getFitness();
            program.getSuspiciousScore();
            return program;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fail to set up inital program");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fail to set up inital program. Unknown error occured");
            System.exit(1);
        }

        return null;
    }

    private Selection<Program> setupBinaryTournamentSelection() {
        return new ProgramBinaryTournamentSlection();
    }

    private SearchAlgorithm<Program> setupSearchAlgorithm(int populationSize, int maxGeneration, double mutationWeight, Selection<Program> selector) {
        return new ClassicGenProgAlgorithm(populationSize, maxGeneration, mutationWeight, selector);
    }

    @Override
    public Integer call() {
        Mutator<Program> mutator = setupClassicMutator(canGetFixFromDifferentClasses);
        Crossover<Program> crossover = setupRawProgramCrossover();
        SuspiciousCalculator suspiciousCalculator;
        faultLocalization = faultLocalization.toLowerCase();
        switch (faultLocalization) {
            case "ochiai": suspiciousCalculator = setupOchiaiSuspiciousCalculator(); break;
            case "tarantula": suspiciousCalculator = setupTarantulaSuspiciousCalculator(); break;
            default: suspiciousCalculator = setupOchiaiSuspiciousCalculator(); break;
        }

        FitnessFunction fitnessFunction =
                setupWeightedFitnessFunction(positiveWeight, negativeWeight);

        Program initialProgram =
                setupInitialProgram(dirPath, classname,
                        mutator, crossover,
                        suspiciousCalculator, fitnessFunction);

        Selection<Program> selection = setupBinaryTournamentSelection();
        SearchAlgorithm<Program> searchAlgorithm =
                setupSearchAlgorithm(populationSize, maxGeneration,
                        mutationWeight, selection);

        long start = System.nanoTime();
        Program result = searchAlgorithm.search(initialProgram);
        System.out.println(result);
        if (result.isMaxFitness()) {
            System.out.println("Patch found!!!");
        } else {
            System.out.println("Fail to find patch, best program is above ^^^");
        }
        long end = System.nanoTime();

        System.out.printf("Elapsed time: %.2f ms%n",
                (end - start) / 1_000_000.0);

        if (outputDir != null) {
            try {
                result.toFile(outputDir);
                System.out.println("Result have been written to " + outputDir);
            } catch (Exception e) {
                System.err.println("Fail to write result to output directory");
                return 0;
            }
        }

        return 0;
    }
}
