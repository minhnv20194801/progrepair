class NotEnoughBalanceException extends Exception {
    public NotEnoughBalanceException(String message) {
        super(message);
    }
}
class Customer {
    protected String name;
    protected double balance = 0.0;
    public Customer(String name, double initialBalance) {
        this.name = name;
        this.balance = initialBalance;
    }
    public void pay(double amount) throws Exception {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be a positive number");
        }
        if (amount > balance) {
            throw new NotEnoughBalanceException("Customer's account can't afford this payment");
        }
        balance -= amount;
    }
    public String getName() {
        return name;
    }
    public double getBalance() {
        return balance;
    }
}
public class VIPCustomer extends Customer {
    public VIPCustomer(String name, double initialBalance) {
        super(name, initialBalance);
    }
    @Override
    public void pay(double amount) throws Exception {
        amount = calculateAmountAfterDiscount(amount);
        super.pay(amount);
    }
    private double calculateAmountAfterDiscount(double amount) {
        return amount * 0.9;
    }
}
