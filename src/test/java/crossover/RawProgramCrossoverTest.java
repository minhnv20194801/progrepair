package crossover;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.group10.crossover.RawProgramCrossover;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.program.Program;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RawProgramCrossoverTest {
    Program intCalculatorProgram;
    SuspiciousCalculator suspiciousCalculator;
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
    String expectedOffspringCodes1 = """
            public class IntCalculator {
                public int add(int a, int b) {
                    return a - b;
                }
                public int subtract(int a, int b) {
                    return a + b;
                }
                public int mul(int a, int b) {
                    return a / b;
                }
                public int add(int a, int b) {
                    return a + b;
                }
                public int subtract(int a, int b) {
                    return a - b;
                }
            }""";

    String expectedOffspringCodes2 = """
            public class IntCalculator {
                public int mul(int a, int b) {
                    return a * b;
                }
                public int div(int a, int b) {
                    if (b == 0) {
                        return 0;
                    }
                    return a * b;
                }
                public int div(int a, int b) {
                    if (b == 0) {
                        return 0;
                    }
                    return a / b;
                }
            }""";

    @BeforeEach
    void setUp() throws Exception {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        StaticJavaParser.setConfiguration(config);

        Randomness.getRandom().setSeed(422);
        suspiciousCalculator = new OchiaiSuspiciousCalculator();
//        shopProgram = new Program("./benchmark/Shop_buggy/", "Shop", mutator, null, suspiciousCalculator, null);
//        shopProgram.executeTestSuite();
        intCalculatorProgram = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", null, new RawProgramCrossover(), suspiciousCalculator, null);
        intCalculatorProgram.executeTestSuite();
    }

    @Test
    void testCrossover() throws Exception {
        List<String> source = Arrays.asList(sourceCode.split("\\R"));
        Program parent2 = new Program(intCalculatorProgram.getClassName(), source, intCalculatorProgram.getTestSuite(), null, null, null, null);

        Map.Entry<Program, Program> offsprings = intCalculatorProgram.crossover(parent2);

        assertEquals(offsprings.getKey().toString(), expectedOffspringCodes1);
        assertEquals(offsprings.getValue().toString(), expectedOffspringCodes2);
    }
}
