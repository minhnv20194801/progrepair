package suspiciouscalculator;

import org.group10.program.Program;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OchiaiSuspiciousCalculatorTest {
    Program buggyProgram;
    SuspiciousCalculator sc;

    @BeforeEach
    void setUp() throws Exception {
        buggyProgram = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", null, null, null, null);
        sc = new OchiaiSuspiciousCalculator();
    }

    @Test
    void testCalculateScore() {
        Map<Integer, Double> scores = sc.calculateScore(buggyProgram);
        assertEquals(scores.get(3), 0.7071067811865475);
        assertEquals(scores.get(7), 0.7071067811865475);
        assertEquals(scores.get(1), 0.6324555320336759);
    }
}
