import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    private Stack<Integer> intStack;
    private Stack<Double> doubleStack;

    @BeforeEach
    void setUp() {
        intStack = new Stack<>();
        doubleStack = new Stack<>();
    }

    @Test
    void testPushAndPeek() {
        intStack.push(10);
        intStack.push(20);

        assertEquals(20, intStack.peek(), "Peek should return the last pushed element");
    }

    @Test
    void testPop() {
        intStack.push(5);
        intStack.push(15);

        int popped = intStack.pop();
        assertEquals(15, popped, "Pop should return the last pushed element");
        assertEquals(5, intStack.peek(), "Peek after pop should return the new top element");
    }

    @Test
    void testIsEmpty() {
        assertTrue(intStack.isEmpty(), "Stack should be empty initially");
        intStack.push(1);
        assertFalse(intStack.isEmpty(), "Stack should not be empty after push");
        intStack.pop();
        assertTrue(intStack.isEmpty(), "Stack should be empty after popping all elements");
    }

    @Test
    void testSize() {
        assertEquals(0, intStack.size(), "Initial size should be 0");
        intStack.push(1);
        intStack.push(2);
        assertEquals(2, intStack.size(), "Size should reflect number of elements");
        intStack.pop();
        assertEquals(1, intStack.size(), "Size should decrease after pop");
    }

    @Test
    void testPopEmptyStackThrowsException() {
        assertThrows(EmptyStackException.class, () -> intStack.pop(), "Pop on empty stack should throw EmptyStackException");
    }

    @Test
    void testPeekEmptyStackThrowsException() {
        assertThrows(EmptyStackException.class, () -> intStack.peek(), "Peek on empty stack should throw EmptyStackException");
    }

    @Test
    void testGenericStackWorksWithDouble() {
        doubleStack.push(1.5);
        doubleStack.push(2.5);

        assertEquals(2.5, doubleStack.peek(), "Peek should work with Double type");
        assertEquals(2.5, doubleStack.pop(), "Pop should work with Double type");
        assertEquals(1.5, doubleStack.peek(), "Peek should return the remaining element");
    }
}
