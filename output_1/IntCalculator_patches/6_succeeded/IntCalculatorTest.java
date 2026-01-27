import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntCalculatorTest {

    private IntCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IntCalculator();
    }

    @Test
    void testAdd() {
        assertEquals(5, calculator.add(2, 3));
        assertEquals(-1, calculator.add(2, -3));
    }

    @Test
    void testSubtract() {
        assertEquals(1, calculator.subtract(3, 2));
        assertEquals(5, calculator.subtract(2, -3));
    }

    @Test
    void testMultiply() {
        assertEquals(6, calculator.mul(2, 3));
        assertEquals(-6, calculator.mul(2, -3));
    }

    @Test
    void testDivide() {
        assertEquals(2, calculator.div(6, 3));
        assertEquals(-2, calculator.div(6, -3));
    }

    @Test
    void testDivideByZero() {
        assertEquals(0, calculator.div(5, 0));
    }
}
