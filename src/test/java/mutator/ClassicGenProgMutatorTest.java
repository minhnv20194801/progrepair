package mutator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.Statement;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.program.Program;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicGenProgMutatorTest {
    Program shopProgram;
    Program intCalculatorProgram;
    ClassicGenProgMutator mutator;
    SuspiciousCalculator suspiciousCalculator;

    @BeforeEach
    void setUp() throws Exception {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        StaticJavaParser.setConfiguration(config);

        Randomness.getRandom().setSeed(1306);
        mutator = new ClassicGenProgMutator();
        suspiciousCalculator = new OchiaiSuspiciousCalculator();
        shopProgram = new Program("./benchmark/Shop_buggy/", "Shop", mutator, null, suspiciousCalculator, null);
        shopProgram.executeTestSuite();
        intCalculatorProgram = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", mutator, null, suspiciousCalculator, null);
        intCalculatorProgram.executeTestSuite();
    }

    @Test
    void testMutateShouldActuallyMutate() {
        for (int i = 0; i < 100; i++) {
            Program mutated = mutator.mutate(shopProgram);
            assert !mutated.equals(shopProgram);
        }
    }

    @Test
    void testMutateShouldInsert() {
        CompilationUnit originalCu = StaticJavaParser.parse(intCalculatorProgram.toString());
        int originalNodeCount = originalCu.findAll(Statement.class).size();
        boolean insertionHappened = false;

        for (int i = 0; i < 100; i++) {
            Program mutated = mutator.mutate(intCalculatorProgram);
            CompilationUnit mutatedCu = StaticJavaParser.parse(mutated.toString());

            int mutatedNodeCount = mutatedCu.findAll(Statement.class).size();

            if (mutatedNodeCount > originalNodeCount) {
                insertionHappened = true;
                break;
            }
        }

        assertTrue(insertionHappened);
    }

    @Test
    void testMutateShouldSwap() {
        CompilationUnit originalCu = StaticJavaParser.parse(intCalculatorProgram.toString());
        int originalNodeCount = originalCu.findAll(Statement.class).size();
        boolean swapHappened = false;

        for (int i = 0; i < 100; i++) {
            Program mutated = mutator.mutate(intCalculatorProgram);
            CompilationUnit mutatedCu = StaticJavaParser.parse(mutated.toString());

            int mutatedNodeCount = mutatedCu.findAll(Statement.class).size();

            if (mutatedNodeCount == originalNodeCount) {
                swapHappened = true;
                break;
            }
        }

        assertTrue(swapHappened);
    }

    @Test
    void testMutateShouldDelete() {
        CompilationUnit originalCu = StaticJavaParser.parse(intCalculatorProgram.toString());
        int originalNodeCount = originalCu.findAll(Statement.class).size();
        boolean deleteHappened = false;

        for (int i = 0; i < 100; i++) {
            Program mutated = mutator.mutate(intCalculatorProgram);
            CompilationUnit mutatedCu = StaticJavaParser.parse(mutated.toString());

            int mutatedNodeCount = mutatedCu.findAll(Statement.class).size();

            if (mutatedNodeCount < originalNodeCount) {
                deleteHappened = true;
                break;
            }
        }

        assertTrue(deleteHappened);
    }
}
