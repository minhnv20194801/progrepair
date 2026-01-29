import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryExprExamplesTest {

    private BinaryExprExamples binaryExprExamples;

    @Test
    void testIsValidAge() {
        BinaryExprExamples binaryExprExamples = new BinaryExprExamples();

        assertTrue(binaryExprExamples.isValidAge(20));
        assertTrue(binaryExprExamples.isValidAge(18));
        assertFalse(binaryExprExamples.isValidAge(16));
    }

    @Test
    void testIsEmpty() {
        BinaryExprExamples binaryExprExamples = new BinaryExprExamples();
        List<String> strLst = new ArrayList<>();

        assertTrue(binaryExprExamples.isEmpty(strLst));

        strLst.add("Hello");
        assertFalse(binaryExprExamples.isEmpty(strLst));
    }
}
