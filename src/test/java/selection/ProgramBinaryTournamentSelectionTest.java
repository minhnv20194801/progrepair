package selection;

import org.group10.fitness.WeightedFitnessFunction;
import org.group10.program.Program;
import org.group10.selection.ProgramBinaryTournamentSelection;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgramBinaryTournamentSelectionTest {
    Program program1;
    Program program2;
    Program fixedProgram;
    WeightedFitnessFunction ff;
    ProgramBinaryTournamentSelection selection;

    String sourceCode2 = """
            public class IntCalculator {
                public int mul(int a, int b) {
                    return a * b;
                }
            
                public int div(int a, int b) {
                    if (b == 0) {
                        return 0;
                    }
            
                    return a / b;
                }

                public int add(int a, int b) {
                    return a - b;
                }
            
                public int subtract(int a, int b) {
                    return a * b;
                }
            }""";

    String fixedSourceCode = """
            public class IntCalculator {
                public int mul(int a, int b) {
                    return a * b;
                }
            
                public int div(int a, int b) {
                    if (b == 0) {
                        return 0;
                    }
            
                    return a / b;
                }

                public int add(int a, int b) {
                    return a + b;
                }
            
                public int subtract(int a, int b) {
                    return a - b;
                }
            }""";

    @BeforeEach
    void setUp() throws Exception {
        Randomness.getRandom().setSeed(13006);
        ff = new WeightedFitnessFunction(1, 10);
        program1 = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", null, null, null, ff);
        List<String> fixedSrcCode = Arrays.asList(fixedSourceCode.split("\\R"));
        List<String> srcCode2 = Arrays.asList(sourceCode2.split("\\R"));
        fixedProgram = new Program(program1.getClassName(), fixedSrcCode, program1.getTestSuite(), null, null, null, ff);
        program2 = new Program(program1.getClassName(), srcCode2, program1.getTestSuite(), null, null, null, ff);
        program1.executeTestSuite();
        program2.executeTestSuite();
        fixedProgram.executeTestSuite();
        selection = new ProgramBinaryTournamentSelection();
    }

    @Test
    void testSelection() {
        List<Program> population = new ArrayList<>();
        population.add(program1);
        population.add(program2);
        population.add(fixedProgram);
        // assert that fitness calculation works as intended
        assertEquals(program1.getFitness(), program2.getFitness());
        assert (fixedProgram.getFitness() > program1.getFitness() && fixedProgram.getFitness() > program2.getFitness());
        Map<Program, Integer> frequency = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            Program victor = selection.select(population);
            if (victor.equals(fixedProgram)) {
                frequency.put(fixedProgram, frequency.getOrDefault(fixedProgram, 0) + 1);
            } else if (victor.equals(program1)) {
                frequency.put(program1, frequency.getOrDefault(program1, 0) + 1);
            } else if (victor.equals(program2)) {
                frequency.put(program2, frequency.getOrDefault(program2, 0) + 1);
            }
        }

        // the frequency of fixedProgram should be biggest
        assert (frequency.get(fixedProgram) > frequency.get(program1) && frequency.get(fixedProgram) > frequency.get(program2));
        // the frequency of program1 and program2 should be similar
        assertTrue(Math.abs(frequency.get(program1) - frequency.get(program2)) <= 5);
    }
}
