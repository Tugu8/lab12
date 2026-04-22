package mn.csm311.lab12;

import mn.csm311.lab12.task1.BankAccount;
import mn.csm311.lab12.task1.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void constructor_rejectsBlankOwner() {
        assertThrows(IllegalArgumentException.class, () -> new BankAccount(null, 1000));
        assertThrows(IllegalArgumentException.class, () -> new BankAccount("", 1000));
        assertThrows(IllegalArgumentException.class, () -> new BankAccount("   ", 1000));
    }

    @Test
    void constructor_rejectsNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> new BankAccount("Bat", -100));
    }

    @Test
    void withdraw_rejectsNonPositiveAmount() {
        BankAccount a = new BankAccount("Bat", 1000);
        assertThrows(IllegalArgumentException.class, () -> a.withdraw(0));
        assertThrows(IllegalArgumentException.class, () -> a.withdraw(-500));
    }

    @Test
    void withdraw_throwsWhenInsufficientFunds() {
        BankAccount a = new BankAccount("Bat", 1000);
        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class,
                () -> a.withdraw(5000));
        assertNotNull(ex.getMessage());
    }

    @Test
    void withdraw_reducesBalance() {
        BankAccount a = new BankAccount("Bat", 1000);
        a.withdraw(300);
        assertEquals(700, a.balance());
    }

    @Test
    void transfer_rejectsNullAccounts() {
        BankAccount a = new BankAccount("Bat", 1000);
        assertThrows(IllegalArgumentException.class, () -> BankAccount.transfer(null, a, 100));
        assertThrows(IllegalArgumentException.class, () -> BankAccount.transfer(a, null, 100));
    }

    @Test
    void transfer_rejectsSameAccount() {
        BankAccount a = new BankAccount("Bat", 1000);
        assertThrows(IllegalArgumentException.class, () -> BankAccount.transfer(a, a, 100));
    }

    @Test
    void transfer_movesMoneyBetweenAccounts() {
        BankAccount from = new BankAccount("Bat", 1000);
        BankAccount to = new BankAccount("Dorj", 500);
        BankAccount.transfer(from, to, 300);
        assertEquals(700, from.balance());
        assertEquals(800, to.balance());
    }

    @Test
    void transfer_doesNotDepositWhenWithdrawFails() {
        BankAccount from = new BankAccount("Bat", 100);
        BankAccount to = new BankAccount("Dorj", 500);
        assertThrows(InsufficientFundsException.class,
                () -> BankAccount.transfer(from, to, 300));
        // Withdraw амжилтгүй болсон бол to-ийн үлдэгдэл өөрчлөгдөхгүй байна
        assertEquals(100, from.balance());
        assertEquals(500, to.balance());
    }
}
