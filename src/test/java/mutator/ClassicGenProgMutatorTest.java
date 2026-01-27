package mutator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.Statement;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.mutator.Mutator;
import org.group10.program.RawSingleFileProgram;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicGenProgMutatorTest {
    RawSingleFileProgram shopProgram;
    RawSingleFileProgram intCalculatorProgram;
    Mutator<RawSingleFileProgram> mutator;
    SuspiciousCalculator suspiciousCalculator;

    @BeforeEach
    void setUp() throws Exception {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        StaticJavaParser.setConfiguration(config);

        Random random = new Random(1306);
        Randomness.setRandom(random);
        mutator = new ClassicGenProgMutator();
        suspiciousCalculator = new OchiaiSuspiciousCalculator();
        shopProgram = new RawSingleFileProgram("./benchmark/Shop_buggy/", "Shop", mutator, null, suspiciousCalculator, null);
        shopProgram.executeTestSuite();
        intCalculatorProgram = new RawSingleFileProgram("./benchmark/IntCalculator_buggy/", "IntCalculator", mutator, null, suspiciousCalculator, null);
        intCalculatorProgram.executeTestSuite();
    }

    @Test
    void testMutateShouldActuallyMutate() {
        for (int i = 0; i < 100; i++) {
            RawSingleFileProgram mutated = mutator.mutate(shopProgram);
            assert !mutated.equals(shopProgram);
        }
    }

    @Test
    void testMutateShouldInsert() {
        CompilationUnit originalCu = StaticJavaParser.parse(intCalculatorProgram.toString());
        int originalNodeCount = originalCu.findAll(Statement.class).size();
        boolean insertionHappened = false;

        for (int i = 0; i < 100; i++) {
            RawSingleFileProgram mutated = mutator.mutate(intCalculatorProgram);
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
            RawSingleFileProgram mutated = mutator.mutate(intCalculatorProgram);
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
            RawSingleFileProgram mutated = mutator.mutate(intCalculatorProgram);
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
