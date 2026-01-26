import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopTest {

    private Shop shop;
    private Customer alice;
    private Customer bob;

    @BeforeEach
    void setUp() {
        shop = createShopWithApple(5);
        alice = new Customer("Alice", new BankAccount(100));
        bob = new Customer("Bob", new BankAccount(100));
    }

    private Shop createShopWithApple(int qty) {
        Item apple = new Item("Apple", qty, 1.0);
        return new Shop(new ArrayList<>(List.of(apple)), 2); // 2 carts for testing
    }

    // ================== Item Tests ==================
    @Test
    void itemEdgeCases() {
        Item item = new Item("Orange", 0, 0.0);
        assertEquals(0.0, item.getTotalPrice(), 0.0001);

        Item negativePrice = new Item("Banana", 2, -5.0);
        assertEquals(-10.0, negativePrice.getTotalPrice(), 0.0001);

        Item zeroQty = new Item("Grapes", 0, 3.0);
        assertEquals(0.0, zeroQty.getTotalPrice(), 0.0001);
    }

    // ================== ShoppingCart Edge Cases ==================
    @Test
    void shoppingCartMultipleItems() {
        ShoppingCart cart = new ShoppingCart();
        Item apple = new Item("Apple", 2, 1.0);
        Item banana = new Item("Banana", 3, 2.0);

        // Add multiple items
        cart.addItem(apple);
        cart.addItem(banana);
        assertEquals(8.0, cart.getTotalPrice(), 0.0001);

        // Remove some banana
        cart.removeItem(new Item("Banana", 2, 2.0));
        assertEquals(4.0, cart.getTotalPrice(), 0.0001);

        // Remove remaining banana
        cart.removeItem(new Item("Banana", 1, 2.0));
        assertEquals(2.0, cart.getTotalPrice(), 0.0001);
    }

    @Test
    void shoppingCartRemoveNonExisting() {
        ShoppingCart cart = new ShoppingCart();
        Item apple = new Item("Apple", 1, 1.0);
        assertNull(cart.removeItem(apple));
    }

    // ================== Customer / VIP Logic ==================
    @Test
    void customerVIPThreshold() throws Exception {
        BankAccount shopAccount = new BankAccount();

        Customer customer = new Customer("VIPTest", new BankAccount(200));

        // Pay exactly 50 -> not VIP
        customer.pay(50, shopAccount);
        assertFalse(customer.isVIP());

        // Pay 50.01 -> VIP
        customer.pay(50.01, shopAccount);
        assertTrue(customer.isVIP());
    }

    @Test
    void customerPayMultipleTimes() throws Exception {
        BankAccount shopAccount = new BankAccount();
        Customer customer = new Customer("Alice", new BankAccount(200));

        customer.pay(30, shopAccount);
        assertFalse(customer.isVIP());

        customer.pay(25, shopAccount);
        assertFalse(customer.isVIP());

        customer.pay(51, shopAccount);
        assertTrue(customer.isVIP());
    }

    // ================== BankAccount Edge Cases ==================
    @Test
    void bankAccountZeroAndNegativeOperations() throws Exception {
        BankAccount account = new BankAccount();
        BankAccount dest = new BankAccount();

        // Transfer zero
        account.transferTo(0, dest);
        assertEquals(0.0, account.getBalance());
        assertEquals(0.0, dest.getBalance());

        // Supply negative ignored
        account.supply(-100);
        assertEquals(0.0, account.getBalance());

        // Supply positive
        account.supply(100);
        assertEquals(100.0, account.getBalance());
    }

    @Test
    void bankAccountMultipleTransfers() throws Exception {
        BankAccount source = new BankAccount(100);
        BankAccount dest = new BankAccount(50);

        source.transferTo(30, dest);
        source.transferTo(20, dest);
        assertEquals(50, source.getBalance(), 0.0001);
        assertEquals(100, dest.getBalance(), 0.0001);
    }

    // ================== Shop Tests ==================
    @Test
    void shopMultipleCustomersAndCarts() throws Exception {
        shop.acceptCustomer(alice);
        shop.acceptCustomer(bob);

        // No more carts -> exception
        Customer charlie = new Customer("Charlie", new BankAccount(100));
        assertThrows(MaximumCustomerException.class, () -> shop.acceptCustomer(charlie));
    }

    @Test
    void shopAddRemoveMultipleItems() throws Exception {
        shop.acceptCustomer(alice);
        shop.acceptCustomer(bob);

        // Add multiple items for alice
        shop.addItemToCustomerCart(alice, new Item("Apple", 2, 1.0));
        shop.addItemToCustomerCart(alice, new Item("Apple", 1, 1.0)); // stock should decrease

        // Add remaining stock for bob
        shop.addItemToCustomerCart(bob, new Item("Apple", 2, 1.0));

        // No stock left -> further addition fails silently
        assertDoesNotThrow(() -> shop.addItemToCustomerCart(alice, new Item("Apple", 1, 1.0)));
    }

    @Test
    void shopCustomerPayingVIPUpdate() throws Exception {
        shop.acceptCustomer(alice);

        // Add items worth >50
        shop.reStock(new Item("Banana", 100, 2.0)); // add expensive items
        shop.addItemToCustomerCart(alice, new Item("Banana", 30, 2.0)); // total = 60

        shop.customerPaying(alice);

        assertTrue(alice.isVIP());
    }

    @Test
    void shopAcceptCustomerShouldReduceCapacity() throws Exception {
        shop.acceptCustomer(alice);
        assertEquals(1, shop.getCapacity(), 0.01);
        shop.acceptCustomer(alice);
        assertEquals(1, shop.getCapacity(), 0.01);
        shop.acceptCustomer(bob);
        assertEquals(0, shop.getCapacity(), 0.01);
    }

    @Test
    void shopCustomerPayingShouldIncreaseCapacity() throws Exception {
        shop.acceptCustomer(alice);

        // Add and remove sequence
        shop.addItemToCustomerCart(alice, new Item("Apple", 2, 1.0));

        // Pay and clear
        shop.customerPaying(alice);
        assertEquals(2, shop.getCapacity(), 0.01);

        // Free cart available again
        shop.acceptCustomer(alice);
        assertEquals(1, shop.getCapacity(), 0.01);
    }

    @Test
    void shopReStockEdgeCases() {
        // Re-stock null
        assertDoesNotThrow(() -> shop.reStock(null));

        // Re-stock zero quantity
        shop.reStock(new Item("Apple", 0, 1.0));
    }

    @Test
    void shopRemoveItemFromStockFullAndPartial() throws Exception {
        shop.acceptCustomer(alice);

        // Partial removal
        shop.addItemToCustomerCart(alice, new Item("Apple", 3, 1.0));

        // Full removal
        Shop smallShop = createShopWithApple(2);
        Customer bob2 = new Customer("Bob2", new BankAccount(100));
        smallShop.acceptCustomer(bob2);
        smallShop.addItemToCustomerCart(bob2, new Item("Apple", 2, 1.0));
    }

    @Test
    void shopMultipleAddRemoveClearSequences() throws Exception {
        shop.acceptCustomer(alice);

        // Add and remove sequence
        shop.addItemToCustomerCart(alice, new Item("Apple", 2, 1.0));
        shop.removeItemFromCustomerCart(alice, new Item("Apple", 1, 1.0));
        shop.addItemToCustomerCart(alice, new Item("Apple", 1, 1.0));

        // Pay and clear
        shop.customerPaying(alice);

        // Free cart available again
        shop.acceptCustomer(bob);
    }

    @Test
    void shopAddItemNotEnoughStockDoesNothing() throws Exception {
        shop.acceptCustomer(alice);

        // Request more than in stock
        shop.addItemToCustomerCart(alice, new Item("Apple", 10, 1.0)); // should not throw
    }

    @Test
    void payingUnregisteredCustomerThrows() {
        Shop shop = createShopWithApple(5);
        Customer c = new Customer("Alice", new BankAccount(100));

        assertThrows(UnregisteredCustomerException.class,
                () -> shop.customerPaying(c));
    }

    @Test
    void addItemUnregisteredCustomerThrows() {
        Shop shop = createShopWithApple(5);
        Customer c = new Customer("Alice", new BankAccount(100));

        assertThrows(UnregisteredCustomerException.class,
                () -> shop.addItemToCustomerCart(c, new Item("Apple", 2, 1.0)));
    }

    @Test
    void removeItemUnregisteredCustomerThrows() {
        Shop shop = createShopWithApple(5);
        Customer c = new Customer("Alice", new BankAccount(100));

        assertThrows(UnregisteredCustomerException.class,
                () -> shop.removeItemFromCustomerCart(c, new Item("Apple", 2, 1.0)));
    }

    @Test
    void transferToNegativeAmount() throws Exception {
        BankAccount source = new BankAccount(50);
        BankAccount dest = new BankAccount(50);

        source.transferTo(-30, dest);

        assertEquals(50, source.getBalance(), 0.0001);
        assertEquals(50, dest.getBalance(), 0.0001);
    }

    @Test
    void transferToMoreThanCurrentBalance() {
        BankAccount source = new BankAccount(50);
        BankAccount dest = new BankAccount(50);

        assertThrows(FailedTransactionException.class, () -> source.transferTo(80, dest));
    }

    @Test
    void newBankAccountNegativeAmount() {
        BankAccount source = new BankAccount(-50);
        assertEquals(0, source.getBalance(), 0.0001);
    }
}
