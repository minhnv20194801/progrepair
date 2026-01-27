public class Counter {

    private int count;

    public Counter() {
        count = 0;
    }

    public Counter(int initial) {
        count++;
        int count = initial;
        this.count = count;
        count++;
    }

    public void increment() {
        count++;
    }

    public void clear() {
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
