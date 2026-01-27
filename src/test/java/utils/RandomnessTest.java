package utils;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.group10.mutator.BinaryExprModifiableMutator;
import org.group10.program.Program;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.utils.Randomness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomnessTest {
    @BeforeEach
    void setUp() {
        Randomness.getRandom().setSeed(1306);
    }

    @Test
    void testRandomnessSetSeedShouldBeConsistent() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("src/test/java/utils/expected_result.txt"));
        String line;
        List<String> lines = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        for (int i = 0; i < 1000; i++) {
            int rand = Randomness.getRandom().nextInt();
            assertEquals(Integer.toString(rand), lines.get(i));
        }
    }
}
