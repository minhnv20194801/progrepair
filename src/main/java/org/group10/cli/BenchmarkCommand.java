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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "benchmark",
        description = "Repair a buggy program using genetic search"
)
public class BenchmarkCommand implements Callable<Integer> {

    private final Map<Integer, Map.Entry<String, String>> benchmarkTargetMap = new HashMap<>();
    @CommandLine.Option(names = {"-b", "--benchmark"}, required = true)
    private int benchmarkTarget;
    @CommandLine.Option(names = {"-r", "--runs"}, defaultValue = "10")
    private int runs;
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

    private void setupBenchmarkTargetMap() {
        benchmarkTargetMap.put(
                0,
                Map.entry("./benchmark/IntCalculator_buggy/", "IntCalculator")
        );

        benchmarkTargetMap.put(
                1,
                Map.entry("./benchmark/Stack_buggy/", "Stack")
        );

        benchmarkTargetMap.put(
                2,
                Map.entry("./benchmark/VIPCustomer_buggy/", "VIPCustomer")
        );

        benchmarkTargetMap.put(
                3,
                Map.entry("./benchmark/Counter_buggy/", "Counter")
        );

        benchmarkTargetMap.put(
                4,
                Map.entry("./benchmark/Shop_buggy/", "Shop")
        );
    }

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
            Program program = new Program(dirPath, className, mutator, crossover, suspiciousCalculator, fitnessFunction);
            program.getFitness();
            program.getSuspiciousScore();

            return program;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Fail to set up inital program");
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
        setupBenchmarkTargetMap();
        Mutator<Program> mutator = setupClassicMutator(canGetFixFromDifferentClasses);
        Crossover<Program> crossover = setupRawProgramCrossover();
        SuspiciousCalculator suspiciousCalculator;
        faultLocalization = faultLocalization.toLowerCase();
        suspiciousCalculator = switch (faultLocalization) {
            case "ochiai" -> setupOchiaiSuspiciousCalculator();
            case "tarantula" -> setupTarantulaSuspiciousCalculator();
            default -> setupOchiaiSuspiciousCalculator();
        };

        FitnessFunction<Program> fitnessFunction =
                setupWeightedFitnessFunction(positiveWeight, negativeWeight);

        String dirPath = benchmarkTargetMap.get(benchmarkTarget).getKey();
        String classname = benchmarkTargetMap.get(benchmarkTarget).getValue();
        Program initialProgram =
                setupInitialProgram(dirPath, classname,
                        mutator, crossover,
                        suspiciousCalculator, fitnessFunction);

        Selection<Program> selection = setupBinaryTournamentSelection();
        SearchAlgorithm<Program> searchAlgorithm =
                setupSearchAlgorithm(populationSize, maxGeneration,
                        mutationWeight, selection);

        int successCount = 0;
        int failCount = 0;
        List<Program> results = new ArrayList<>();
        List<Double> runTimes = new ArrayList<>();
        List<Double> successRunTimes = new ArrayList<>();
        System.out.println("=====================================================================");
        System.out.println("Starting benchmark for target: " + initialProgram.getClassName());
        System.out.println("Benchmark Setting: ");
        System.out.println("#runs: " + runs);
        System.out.println("=====================================================================");
        for (int i = 0; i < runs; i++) {
            System.out.println("====================================================");
            System.out.println("Benchmark Run #" + i);
            long start = System.nanoTime();
            Program result = searchAlgorithm.search(initialProgram);
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1_000_000.0;
            if (result.isMaxFitness()) {
                successCount++;
                successRunTimes.add(elapsedTime);
            } else {
                failCount++;
            }
            runTimes.add(elapsedTime);
            results.add(result);
        }

        double median;
        double successMedian = 0.0;
        int n = runTimes.size();

        runTimes = runTimes.stream()
                .sorted()
                .toList();

        successRunTimes = successRunTimes.stream()
                .sorted()
                .toList();

        if (n % 2 == 1) {
            median = runTimes.get(n / 2);
        } else {
            median = (runTimes.get(n / 2 - 1) + runTimes.get(n / 2)) / 2.0;
        }

        n = successRunTimes.size();
        if (n > 0) {
            if (n % 2 == 1) {
                successMedian = successRunTimes.get(n / 2);
            } else {
                successMedian = (successRunTimes.get(n / 2 - 1) + successRunTimes.get(n / 2)) / 2.0;
            }
        }

        System.out.println("=====================================================================");
        System.out.println("Benchmark Summary:");
        System.out.println("=====================================================================");
        System.out.println("Successful run count:" + successCount);
        System.out.println("Failed run count:" + failCount);
        System.out.println("Best time: " + runTimes.getFirst() + "ms");
        System.out.println("Worst time: " + runTimes.getLast() + "ms");
        if (successCount > 0) {
            System.out.println("Worst time (success): " + successRunTimes.getLast() + "ms");
        }
        System.out.println("Average time: " + runTimes.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN) + "ms");
        if (successCount > 0) {
            System.out.println("Average time (success): " + successRunTimes.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN) + "ms");
        }
        System.out.println("Median time: " + median + "ms");
        if (successCount > 0) {
            System.out.println("Median time (success): " + successMedian + "ms");
        }
        System.out.println("=====================================================================");

        if (outputDir != null) {
            try {
                for (int i = 0; i < results.size(); i++) {
                    Program result = results.get(i);
                    String dir = outputDir + "/" + i + "_";
                    if (result.isMaxFitness()) {
                        dir += "succeeded/";
                    } else {
                        dir += "failed/";
                    }
                    result.toFile(dir);
                }
                System.out.println("Results have been written to " + outputDir);
            } catch (Exception e) {
                System.err.println("Fail to write result to output directory");
                return 0;
            }
        }
        return 0;
    }
}
