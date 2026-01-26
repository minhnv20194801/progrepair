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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Java program used in a genetic programming. <br>
 *
 * The class contains the source code of a Java class along with its
 * associated test suite, mutator, crossover operator, suspiciousness calculator
 * (fault localization), and fitness function.
 */
public class Program implements Cloneable {
    private final String className;
    private final Mutator<Program> mutator;
    private final Crossover<Program> crossover;
    private final SuspiciousCalculator suspiciousCalculator;
    private final FitnessFunction<Program> fitnessFunction;
    private final Map<Integer, Integer> efs = new HashMap<>();
    private final Map<Integer, Integer> nfs = new HashMap<>();
    private final Map<Integer, Integer> eps = new HashMap<>();
    private final Map<Integer, Integer> nps = new HashMap<>();
    private final TestSuite testSuite;
    private List<String> codes = new ArrayList<>();
    private List<String> positiveTests = new ArrayList<>();
    private List<String> negativeTests = new ArrayList<>();
    private boolean isTestSuiteExecuted = false;

    /**
     * Construct a Program instance with the following parameters:
     *
     * @param className the name of the class represented by this program
     * @param codes the source code of the program as a list of lines
     * @param testSuite the {@link TestSuite} containing the tests for this program
     * @param mutator the {@link Mutator} used to apply mutations to this program
     * @param crossover the {@link Crossover} operator used to combine this program with others
     * @param suspiciousCalculator the {@link SuspiciousCalculator} used to compute suspiciousness scores for statements
     * @param fitnessFunction the {@link FitnessFunction} used to evaluate the quality of this program
     */
    public Program(String className, List<String> codes, TestSuite testSuite,
                   Mutator<Program> mutator, Crossover<Program> crossover,
                   SuspiciousCalculator suspiciousCalculator,
                   FitnessFunction<Program> fitnessFunction) {
        this.className = className;
        this.codes = codes;
        this.testSuite = testSuite;
        this.mutator = mutator;
        this.crossover = crossover;
        this.suspiciousCalculator = suspiciousCalculator;
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * Constructs a new {@link Program} by reading its source code from a file. <br>
     *
     * This constructor reads the program's Java source file and initializes its
     * test suite from the corresponding test file in the specified directory. <br>
     *
     * NOTE: the name of the java file should be "{@code className}.java", but then
     * all Java source file should be like that.
     *
     * @param dirPath the directory path where the program and test files are located
     * @param className the name of the class (without ".java") to load
     * @param mutator the {@link Mutator} used to apply mutations to this program
     * @param crossover the {@link Crossover} operator used to combine this program with others
     * @param suspiciousCalculator the {@link SuspiciousCalculator} used to compute suspiciousness scores
     * @param fitnessFunction the {@link FitnessFunction} used to evaluate this program
     * @throws IOException if an I/O error occurs while reading the program source file or test file
     */
    public Program(String dirPath, String className, Mutator<Program> mutator,
                   Crossover<Program> crossover, SuspiciousCalculator suspiciousCalculator,
                   FitnessFunction<Program> fitnessFunction) throws IOException {
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

    /**
     * Applies the program's {@link Mutator} to produce a mutated version of this program. <br>
     *
     * @return a mutated {@code Program} instance
     */
    public Program mutate() {
        return mutator.mutate(this);
    }

    /**
     * Applies the program's {@link Crossover} with another parent
     * to produce a pair of offspring. <br>
     *
     * @param parent2 the entity to perform crossover with
     * @return a {@link Map.Entry} represent the pair of produced offspring
     */
    public Map.Entry<Program, Program> crossover(Program parent2) {
        return crossover.crossover(this, parent2);
    }

    /**
     * Checks whether the program can be compiled successfully. <br>
     *
     * In case any exception happened, then the program will be considered not compilable.
     * Thus the result of this method will be {@code false}.
     *
     * @return {@code true} if the program fails to compile, {@code false} otherwise
     */
    public boolean isNotCompilable() {
        try {
            long id = ProcessHandle.current().pid();
            Path outputDir = Files.createTempDirectory(className + id + "compiled_");

            Path javaFile = outputDir.resolve(className + ".java");
            Files.write(javaFile, codes);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                FolderCleaner.cleanTmpDir(className);
                return true;
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

    /**
     * Executes the associated test suite for this program. <br>
     *
     * @throws Exception if an error occurs during test execution
     */
    public void executeTestSuite() throws Exception {
        executeTestSuite(false);
    }

    /**
     * Executes the associated test suite for this program, but also print
     * out the test result log. <br>
     *
     * @throws Exception if an error occurs during test execution
     */
    public void executeTestSuiteWithLog() throws Exception {
        executeTestSuite(true);
    }

    /**
     * Executes the associated test suite for this program, log are optional. <br>
     *
     * @param withLog boolean flag to see if log should be print or not
     * @throws Exception if an error occurs during test execution
     */
    private void executeTestSuite(boolean withLog) throws Exception {
        if (isTestSuiteExecuted) return;
        testSuite.executeTests(this, withLog);
        isTestSuiteExecuted = true;
    }

    /**
     * Writes the program's source code and its associated test suite to files. <br>
     *
     * The program source code is saved as {@code <className>.java} in the specified
     * output directory. The directory is created if it does not already exist. <br>
     * The test suite is also written to the same directory using
     * {@link org.group10.testsuite.TestSuite#toFile(String, String)}.
     *
     * @param outputDir the directory where the program and test suite files will be saved
     * @throws Exception if an I/O error occurs while creating directories or writing files
     */
    public void toFile(String outputDir) throws Exception {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        Path javaFile = dir.resolve(className + ".java");
        Files.write(javaFile, codes);
        this.testSuite.toFile(outputDir, this.getClassName());
    }

    /**
     * Return the source code of the program as a {@link String}.
     * @return the source code of the program
     */
    @Override
    public String toString() {
        return String.join("\n", codes);
    }

    /**
     * Check if two programs are equal based solely on the source codes.
     *
     * @param obj object to be compared with
     * @return {@code true} if two programs source codes are identical
     *         {@code false} otherwise
     */
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

    /**
     * Getter for the program's className.
     * @return the class name of the program
     */
    public String getClassName() {
        return className;
    }

    /**
     * Getter for the program's source code.
     * @return the source code of the program with each lines stored as an
     * element of a {@link List<String>}
     */
    public List<String> getCodes() {
        return codes;
    }

    /**
     * Getter for the program's number of failed test.
     * @return the number of failed test of the program
     */
    public long getTestFailedCount() {
        return negativeTests.size();
    }

    /**
     * Getter for the program's {@link List} of failed tests.
     * @return the list of program's failed tests (only the name of the tests)
     */
    public List<String> getNegativeTests() {
        return negativeTests;
    }

    /**
     * Setter for the program's {@link List} of failed tests.
     * @param negativeTests the list of failed tests to be set
     */
    public void setNegativeTests(List<String> negativeTests) {
        this.negativeTests = negativeTests;
    }

    /**
     * Getter for the program's number of successful test.
     * @return the number of successful test of the program
     */
    public long getTestSuccessfulCount() {
        return positiveTests.size();
    }

    /**
     * Getter for the program's {@link List} of successful tests.
     * @return the list of program's successful tests (only the name of the tests)
     */
    public List<String> getPositiveTests() {
        return positiveTests;
    }

    /**
     * Setter for the program's {@link List} of successful tests.
     * @param positiveTests the list of successful tests to be set
     */
    public void setPositiveTests(List<String> positiveTests) {
        this.positiveTests = positiveTests;
    }

    /**
     * Getter for the program size (Line of codes)
     * @return the number of line of codes in the program
     */
    public int getSize() {
        return codes.size();
    }

    /**
     * Getter for the map of "executed in failed tests" (EF) counts
     * for each line of the program.
     * @return {@link Map} where the key contains the line number
     * and value contains number of execution
     */
    public Map<Integer, Integer> getEfs() {
        return efs;
    }

    /**
     * Getter for the map of "not executed in failed tests" (NF) counts
     * for each line of the program.
     * @return {@link Map} where the key contains the line number
     * and value contains number of execution
     */
    public Map<Integer, Integer> getNfs() {
        return nfs;
    }

    /**
     * Getter for the map of "executed in passed (successful) tests" (EP) counts
     * for each line of the program.
     * @return {@link Map} where the key contains the line number
     * and value contains number of execution
     */
    public Map<Integer, Integer> getEps() {
        return eps;
    }

    /**
     * Getter for the map of "not executed in passed (successful) tests" (NP) counts
     * for each line of the program.
     * @return {@link Map} where the key contains the line number
     * and value contains number of execution
     */
    public Map<Integer, Integer> getNps() {
        return nps;
    }

    /**
     * Perform fault localization on the program to get the suspicious scores.
     * @return {@link Map} where the key contains the line number
     * and the value contains its suspicious score
     */
    public Map<Integer, Double> getSuspiciousScore() {
        return suspiciousCalculator.calculateScore(this);
    }

    /**
     * Getter for the program's mutator
     * @return the mutator of the program
     */
    public Mutator<Program> getMutator() {
        return mutator;
    }

    /**
     * Getter for the program's suspicious score calculator (fault localization method)
     * @return the suspicious score calculator of the program
     */
    public SuspiciousCalculator getSuspiciousCalculator() {
        return suspiciousCalculator;
    }

    /**
     * Getter for the program test suite
     * @return the test suite of the program
     */
    public TestSuite getTestSuite() {
        return testSuite;
    }

    /**
     * Getter for the program crossover
     * @return the crossover of the program
     */
    public Crossover<Program> getCrossover() {
        return crossover;
    }

    /**
     * Getter for the program fitness function
     * @return the fitness function of the program
     */
    public FitnessFunction<Program> getFitnessFunction() {
        return fitnessFunction;
    }

    /**
     * Perform fitness calculation to get the fitness value
     * of the program
     * @return the fitness value of the program
     */
    public double getFitness() {
        return fitnessFunction.calculateFitness(this);
    }

    /**
     * Check if the program fitness is at maximum value
     * @return {@code true} if the program is at maximum fitness value
     *         {@code false} otherwise
     */
    public boolean isMaxFitness() {
        return fitnessFunction.isAtMaxValue(this);
    }

    /**
     * Create a cloned version of the program with similar test suite,
     * mutator, crossover, fault localization and fitness function
     * @return the cloned version of the program
     */
    @Override
    public Program clone() {
        try {
            super.clone();
            return new Program(this.className, this.codes, this.testSuite, this.mutator, this.crossover, this.suspiciousCalculator, this.fitnessFunction);
        } catch (Exception e) {
            return null;
        }
    }
}

