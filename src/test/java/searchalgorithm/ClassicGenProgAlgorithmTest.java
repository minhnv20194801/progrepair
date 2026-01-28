package searchalgorithm;

import org.group10.crossover.Crossover;
import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.FitnessFunction;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.mutator.Mutator;
import org.group10.program.Program;
import org.group10.searchalgorithm.ClassicGenProgAlgorithm;
import org.group10.selection.ProgramBinaryTournamentSelection;
import org.group10.selection.Selection;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.*;

public class ClassicGenProgAlgorithmTest {
    ClassicGenProgAlgorithm classicGenProgAlgorithm;

    @BeforeEach
    void setUp() throws Exception {
        Randomness.getRandom().setSeed(13006);
        Selection<Program> selector = new ProgramBinaryTournamentSelection();
        // populationSize: 40, maxGenerations: 10 so the tests does not take forever
        classicGenProgAlgorithm = new ClassicGenProgAlgorithm(40, 10, 0.06, selector);
    }

    Program setUpProgram(String dirPath, String className) throws Exception {
        Mutator<Program> mutator = new ClassicGenProgMutator();
        Crossover<Program> crossover = new RawProgramCrossover();
        SuspiciousCalculator sc = new OchiaiSuspiciousCalculator();
        FitnessFunction<Program> ff = new WeightedFitnessFunction(1.0, 10.0);

        return new Program(dirPath, className, mutator, crossover, sc, ff);
    }

    @Test
    void testSearchShouldSuccess() throws Exception {
        Program program = setUpProgram("./benchmark/IntCalculator_buggy/", "IntCalculator");
        tapSystemOut(() -> {
            Program result = classicGenProgAlgorithm.search(program);
            assertTrue(result.isMaxFitness());
            assertEquals(result.getTestSuccessfulCount(), 5);
            assertEquals(result.getTestFailedCount(), 0);
        });

    }

    @Test
    void testSearchShouldNotHangNorCrash() throws Exception{
        Program program = setUpProgram("./benchmark/Shop_buggy/", "Shop");

        tapSystemOut(() -> {
            assertTimeoutPreemptively(Duration.ofSeconds(300), () -> {
                classicGenProgAlgorithm.search(program);
            });
        });
    }

    @Test
    void testSearchShouldNotHangNorCrash2() throws Exception{
        // BinaryExprExamples is a very simple program with 0 compilable mutation possible
        // I suppose this is here as an edge case
        // I actually have to fix the search algorithm quite a bit because it crashed on this benchmark
        Program program = setUpProgram("./benchmark/BinaryExprExamples_buggy/", "BinaryExprExamples");

        tapSystemOut(() -> {
            assertTimeoutPreemptively(Duration.ofSeconds(300), () -> {
                classicGenProgAlgorithm.search(program);
            });
        });
    }
}
