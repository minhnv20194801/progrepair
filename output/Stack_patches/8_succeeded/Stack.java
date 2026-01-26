import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class Stack<T> {

    private final List<T> stack = new ArrayList<>();

    public T pop() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        return stack.removeLast();
    }

    public void push(T element) {
        stack.add(element);
    }

    public T peek() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        return stack.getLast();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
