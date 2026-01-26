import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

    private Counter counter;

    @Test
    void testDefaultConstructor() {
        counter = new Counter();
        assertEquals(0, counter.getCount(), "Default constructor should set count to 0");
    }

    @Test
    void testInitialValueConstructor() {
        Counter c = new Counter(10);
        assertEquals(10, c.getCount(), "Constructor with initial value should set count correctly");
    }

    @Test
    void testIncrement() {
        counter = new Counter();
        counter.increment();
        assertEquals(1, counter.getCount(), "Increment should increase count by 1");

        counter.increment();
        assertEquals(2, counter.getCount(), "Increment should work multiple times");
    }

    @Test
    void testClear() {
        counter = new Counter();
        counter.setCount(5);
        counter.clear();
        assertEquals(0, counter.getCount(), "Clear should reset count to 0");
    }

    @Test
    void testSetCount() {
        counter = new Counter();
        counter.setCount(7);
        assertEquals(7, counter.getCount(), "setCount should update the count correctly");
    }

    @Test
    void testIncrementAfterSetCount() {
        counter = new Counter();
        counter.setCount(3);
        counter.increment();
        assertEquals(4, counter.getCount(), "Increment after setCount should work correctly");
    }
}
