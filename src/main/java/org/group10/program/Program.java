package org.group10.program;

import org.group10.crossover.Crossover;
import org.group10.fitness.FitnessFunction;
import org.group10.mutator.Mutator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.testsuite.TestSuite;
import org.group10.utils.DummyOutputStream;
import org.group10.utils.FolderCleaner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Program implements Cloneable {
    private final String className;
    private List<String> codes = new ArrayList<>();
    private final Mutator<Program> mutator;
    private final Crossover<Program> crossover;
    private final SuspiciousCalculator suspiciousCalculator;
    private final FitnessFunction<Program> fitnessFunction;
    private TestSuite testSuite;

    private List<String> positiveTests = new ArrayList<>();
    private List<String> negativeTests = new ArrayList<>();
    private final Map<Integer, Integer> efs = new HashMap<>();
    private final Map<Integer, Integer> nfs = new HashMap<>();
    private final Map<Integer, Integer> eps = new HashMap<>();
    private final Map<Integer, Integer> nps = new HashMap<>();

    private boolean isTestSuiteExecuted = false;

    public Program(String className, List<String> codes, TestSuite testSuite, Mutator<Program> mutator, Crossover<Program> crossover, SuspiciousCalculator suspiciousCalculator, FitnessFunction<Program> fitnessFunction) {
        this.className = className;
        this.codes = codes;
        this.testSuite = testSuite;
        this.mutator = mutator;
        this.crossover = crossover;
        this.suspiciousCalculator = suspiciousCalculator;
        this.fitnessFunction = fitnessFunction;
    }

    public Program(String dirPath, String className, Mutator<Program> mutator, Crossover<Program> crossover, SuspiciousCalculator suspiciousCalculator, FitnessFunction<Program> fitnessFunction) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dirPath + className + ".java"));
        String line;
        while ((line = br.readLine()) != null) {
            codes.add(line);
        }

        this.className = className;
        this.mutator = mutator;
        this.crossover = crossover;
        this.testSuite = new TestSuite(dirPath + className + "Test.java");
        this.suspiciousCalculator = suspiciousCalculator;
        this.fitnessFunction = fitnessFunction;
    }

    public Program mutate() {
        return mutator.mutate(this);
    }

    public Map.Entry<Program, Program> crossover(Program parent2) {
        return crossover.crossover(this, parent2);
    }

    public boolean isNotCompilable() {
        try {
            long id = ProcessHandle.current().pid();
            Path outputDir = Files.createTempDirectory(className+id+"compiled_");

            Path javaFile = outputDir.resolve(className + ".java");
            Files.write(javaFile, codes);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                FolderCleaner.cleanTmpDir(className);
                throw new IllegalStateException("No Java compiler available (JRE instead of JDK)");
            }

            // dummyOutputStream so the compiler error or warning does not go out
            OutputStream dummyOutputStream = new DummyOutputStream();

            int result = compiler.run(
                    null,
                    dummyOutputStream,
                    dummyOutputStream,
                    javaFile.toString()
            );

            FolderCleaner.cleanTmpDir(className);
            return result != 0;
        } catch (Exception e) {
            FolderCleaner.cleanTmpDir(className);
            return true;
        }
    }

    public void executeTestSuite() throws Exception {
        executeTestSuite(false);
    }

    public void executeTestSuiteWithLog() throws Exception {
        executeTestSuite(true);
    }

    private void executeTestSuite(boolean withLog) throws Exception {
        if (isTestSuiteExecuted) return;
        testSuite.executeTests(this, withLog);
        isTestSuiteExecuted = true;
    }

    public void toFile(String outputDir) throws Exception {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        Path javaFile = dir.resolve(className + ".java");
        Files.write(javaFile, codes);
        this.testSuite.toFile(outputDir, this.getClassName());
    }

    @Override
    public String toString() {
        return String.join("\n", codes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Program prog) {
            String code1 = prog.codes.stream()
                    .filter(line -> line != null && !line.isBlank())
                    .collect(Collectors.joining("\n"));

            String code2 = this.codes.stream()
                    .filter(line -> line != null && !line.isBlank())
                    .collect(Collectors.joining("\n"));

            return code1.equals(code2);
        }
        return super.equals(obj);
    }

    public String getClassName() {
        return className;
    }

    public List<String> getCodes() {
        return codes;
    }

    public long getTestFailedCount() {
        return negativeTests.size();
    }
    public List<String> getNegativeTests() {
        return negativeTests;
    }
    public void setNegativeTests(List<String> negativeTests) {
        this.negativeTests = negativeTests;
    }
    public long getTestSuccessfulCount() {
        return positiveTests.size();
    }
    public List<String> getPositiveTests() {
        return positiveTests;
    }
    public void setPositiveTests(List<String> positiveTests) {
        this.positiveTests = positiveTests;
    }

    public int getSize() {
        return codes.size();
    }

    public Map<Integer, Integer> getEfs() {
        return efs;
    }

    public Map<Integer, Integer> getNfs() {
        return nfs;
    }

    public Map<Integer, Integer> getEps() {
        return eps;
    }

    public Map<Integer, Integer> getNps() {
        return nps;
    }

    public Map<Integer, Double> getSuspiciousScore() {
        return suspiciousCalculator.calculateScore(this);
    }

    public Mutator<Program> getMutator() {
        return mutator;
    }

    public SuspiciousCalculator getSuspiciousCalculator() {
        return suspiciousCalculator;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public Crossover<Program> getCrossover() {
        return crossover;
    }

    public FitnessFunction<Program> getFitnessFunction() {
        return fitnessFunction;
    }

    public double getFitness() {
        return fitnessFunction.calculateFitness(this);
    }

    public boolean isMaxFitness() {
        return fitnessFunction.isMax(this);
    }

    @Override
    public Program clone() {
        try {
            return new Program(this.className, this.codes, this.testSuite, this.mutator, this.crossover, this.suspiciousCalculator, this.fitnessFunction);
        } catch (Exception e) {
            return null;
        }
    }
}

