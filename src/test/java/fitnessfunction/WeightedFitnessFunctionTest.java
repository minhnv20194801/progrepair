package fitnessfunction;

import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.program.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeightedFitnessFunctionTest {
    Program program;
    Program fixedProgram;
    WeightedFitnessFunction ff;

    String sourceCode = """
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
            }
            """;

    @BeforeEach
    void setup() throws Exception {
        ff = new WeightedFitnessFunction(1, 10);
        program = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", null, null, null, ff);
        List<String> fixedSrcCode = Arrays.asList(sourceCode.split("\\R"));
        fixedProgram = new Program(program.getClassName(), fixedSrcCode, program.getTestSuite(), null, null, null, ff);
        program.executeTestSuite();
        fixedProgram.executeTestSuite();
    }

    @Test
    void testGetFitness() {
        assertEquals(ff.calculateFitness(program), 3.0);
        assertEquals(ff.calculateFitness(fixedProgram), 23.0);
        assertTrue(ff.isAtMaxValue(fixedProgram));
        assertFalse(ff.isAtMaxValue(program));
    }
}
