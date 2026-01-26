import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VIPCustomerTest {

    @Test
    void testCustomerNormalPayment() throws Exception {
        Customer customer = new Customer("Alice", 100.0);
        customer.pay(50.0);
        assertEquals(50.0, customer.getBalance());
    }

    @Test
    void testCustomerInsufficientBalance() {
        Customer customer = new Customer("Bob", 30.0);

        NotEnoughBalanceException exception = assertThrows(
                NotEnoughBalanceException.class,
                () -> customer.pay(50.0)
        );

        assertEquals("Customer's account can't afford this payment", exception.getMessage());
    }

    @Test
    void testCustomerNegativePayment() {
        Customer customer = new Customer("Charlie", 100.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customer.pay(-20.0)
        );

        assertEquals("Amount must be a positive number", exception.getMessage());
    }

    @Test
    void testVIPCustomerPaymentWithDiscount() throws Exception {
        VIPCustomer vip = new VIPCustomer("Diana", 100.0);
        vip.pay(50.0); // 10% discount → 45.0 charged

        assertEquals(55.0, vip.getBalance(), 0.0001);
    }

    @Test
    void testVIPCustomerInsufficientBalanceAfterDiscount() {
        VIPCustomer vip = new VIPCustomer("Eve", 40.0);

        NotEnoughBalanceException exception = assertThrows(
                NotEnoughBalanceException.class,
                () -> vip.pay(50.0) // discounted amount = 45 → exceeds 40
        );

        assertEquals("Customer's account can't afford this payment", exception.getMessage());
    }
}
