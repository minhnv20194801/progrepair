package mutator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import org.group10.mutator.BinaryExprModifiableMutator;
import org.group10.program.Program;
import org.group10.program.Program;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BinaryExprModifiableMutatorTest {
    Program shopProgram;
    Program intCalculatorProgram;
    BinaryExprModifiableMutator mutator;
    SuspiciousCalculator suspiciousCalculator;

    @BeforeEach
    void setUp() throws Exception {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        StaticJavaParser.setConfiguration(config);

        Randomness.getRandom().setSeed(1306);
        mutator = new BinaryExprModifiableMutator();
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
    void testMutateShouldModifyBinaryExpr() {
        CompilationUnit originalCu = StaticJavaParser.parse(intCalculatorProgram.toString());
        boolean changed = false;
        for (int i = 0; i < 100; i++) {
            Program mutated = mutator.mutate(intCalculatorProgram);
            CompilationUnit mutatedCu = StaticJavaParser.parse(mutated.toString());
            List<BinaryExpr> originalExprs = originalCu.findAll(BinaryExpr.class);
            List<BinaryExpr> mutatedExprs = mutatedCu.findAll(BinaryExpr.class);
            // Insert could have occurred
            if (originalExprs.size() != mutatedExprs.size()) {
                continue;
            }
            for (int j = 0; j < originalExprs.size(); j++) {
                if (originalExprs.get(j).getOperator() != mutatedExprs.get(j).getOperator()) {
                    changed = true;
                    break;
                }
            }
            if (changed) {
                break;
            }
        }

        assertTrue(changed);
    }
}
