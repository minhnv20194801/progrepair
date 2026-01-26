import java.util.*;

class Item {

    private String name;

    private int quantity;

    private double pricePerUnit;

    public Item(String name, int quantity, double pricePerUnit) {
        this.name = name;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item other) {
            return this.name.equals(other.name);
        }
        return super.equals(obj);
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return quantity * pricePerUnit;
    }
}

class ShoppingCart {

    private final List<Item> items = new ArrayList<>();

    public ShoppingCart() {
    }

    public void addItem(Item item) {
        int index = items.indexOf(item);
        if (index == -1) {
            items.add(item);
            return;
        }
        Item itemTobeAdded = items.get(index);
        itemTobeAdded.setQuantity(itemTobeAdded.getQuantity() + item.getQuantity());
    }

    public Item removeItem(Item item) {
        if (!items.contains(item)) {
            return null;
        }
        Item itemTobeRemoved = items.get(items.indexOf(item));
        if (itemTobeRemoved.getQuantity() > item.getQuantity()) {
            itemTobeRemoved.setQuantity(itemTobeRemoved.getQuantity() - item.getQuantity());
            return item;
        } else {
            items.remove(itemTobeRemoved);
            item.setQuantity(itemTobeRemoved.getQuantity());
            return item;
        }
    }

    public void clear() {
        items.clear();
    }

    public double getTotalPrice() {
        double total = 0;
        for (Item item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}

class Customer {

    private String name;

    private BankAccount bankAccount;

    private boolean isVIP = false;

    public Customer(String name, BankAccount bankAccount) {
        this.name = name;
        this.bankAccount = bankAccount;
    }

    public void pay(double amount, BankAccount to) throws FailedTransactionException {
        this.bankAccount.transferTo(amount, to);
        if (amount > 50) {
            this.isVIP = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Customer other) {
            return this.name.equals(other.name);
        }
        return super.equals(obj);
    }

    public boolean isVIP() {
        return isVIP;
    }
}

class FailedTransactionException extends Exception {

    public FailedTransactionException(String message) {
        super(message);
    }
}

class BankAccount {

    private double balance;

    public BankAccount() {
    }

    public BankAccount(double balance) {
        if (balance < 0) {
            balance = 0;
        }
        this.balance = balance;
        this.balance = balance;
    }

    public void supply(double amount) {
        if (amount < 0) {
            amount = 0;
        }
        this.balance += amount;
    }

    public void transferTo(double amount, BankAccount destination) throws FailedTransactionException {
        if (amount > balance) {
            throw new FailedTransactionException("Bank account don't have enough money");
        }
        if (amount < 0) {
            amount = 0;
        }
        this.balance -= amount;
        destination.balance += amount;
    }

    protected double getBalance() {
        return balance;
    }
}

class MaximumCustomerException extends Exception {

    public MaximumCustomerException() {
        super("The maximum customer capaity have been reached");
    }
}

class UnregisteredCustomerException extends Exception {

    public UnregisteredCustomerException() {
        super("The customer is not registered");
    }
}

public class Shop {

    private BankAccount bankAccount = new BankAccount();

    private List<Item> items;

    private List<ShoppingCart> freeCarts = new LinkedList<>();

    private Map<Customer, ShoppingCart> activeCustomerMap = new HashMap<>();

    public Shop(List<Item> initialItems, int maxNumberOfCarts) {
        items = initialItems;
        for (int i = 0; i < maxNumberOfCarts; i++) {
            freeCarts.add(new ShoppingCart());
        }
    }

    public void reStock(Item item) {
        if (item == null) {
            return;
        }
        int index = items.indexOf(item);
        if (index == -1) {
            items.add(item);
        } else {
            Item oldItem = items.get(index);
            oldItem.setQuantity(oldItem.getQuantity() + item.getQuantity());
        }
    }

    public void addItemToCustomerCart(Customer customer, Item item) throws UnregisteredCustomerException {
        ShoppingCart customerCart = activeCustomerMap.get(customer);
        if (customerCart == null) {
            throw new UnregisteredCustomerException();
        }
        if (isItemInStock(item)) {
            Item removedItem = removeItemFromStock(item);
            customerCart.addItem(removedItem);
        }
    }

    public void removeItemFromCustomerCart(Customer customer, Item item) throws UnregisteredCustomerException {
        ShoppingCart customerCart = activeCustomerMap.get(customer);
        if (customerCart == null) {
            throw new UnregisteredCustomerException();
        }
        Item removedItem = customerCart.removeItem(item);
        reStock(removedItem);
    }

    public void acceptCustomer(Customer customer) throws MaximumCustomerException {
        if (activeCustomerMap.get(customer) != null) {
            return;
        }
        if (freeCarts.isEmpty()) {
            throw new MaximumCustomerException();
        }
        ShoppingCart freeCart = freeCarts.getFirst();
        freeCarts.removeFirst();
        activeCustomerMap.put(customer, freeCart);
    }

    public void customerPaying(Customer customer) throws FailedTransactionException, UnregisteredCustomerException {
        ShoppingCart customerCart = activeCustomerMap.get(customer);
        if (customerCart == null) {
            throw new UnregisteredCustomerException();
        }
        double amount = customerCart.getTotalPrice();
        customer.pay(amount, bankAccount);
        activeCustomerMap.remove(customer);
        customerCart.clear();
        freeCarts.add(customerCart);
    }

    private Item removeItemFromStock(Item item) {
        if (!items.contains(item)) {
            return null;
        }
        Item stockItem = items.get(items.indexOf(item));
        if (stockItem.getQuantity() > item.getQuantity()) {
            stockItem.setQuantity(stockItem.getQuantity() - item.getQuantity());
            return item;
        } else {
            items.remove(stockItem);
            item.setQuantity(stockItem.getQuantity());
            return item;
        }
    }

    public int getCapacity() {
        return freeCarts.size();
    }

    private boolean isItemInStock(Item item) {
        int index = items.indexOf(item);
        return index != -1 && (items.get(index).getQuantity() >= item.getQuantity());
    }
}
