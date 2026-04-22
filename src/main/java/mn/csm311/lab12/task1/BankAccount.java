package mn.csm311.lab12.task1;

/**
 * Энгийн банкны дансны анги.
 *
 * ДААЛГАВАР 1: Энэ анги бат бөх БИШ.
 *  - Ямар ч оролтын шалгалтгүй
 *  - Null объектуудад хандана
 *  - Дансны invariant (balance >= 0) зөрчигдөж болно
 *
 * Таны даалгавар: Fail-fast зарчим, Design by Contract (precondition)
 * ашиглан энэ ангийг сайжрах. Preconditions зөрчигдвөл тодорхой
 * exception шидэх.
 */
public class BankAccount {

    private final String owner;
    private long balance; // MNT

    public BankAccount(String owner, long initialBalance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner must not be blank");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("initialBalance must be non-negative");
        }
        this.owner = owner;
        this.balance = initialBalance;
    }

    public String owner() {
        return owner;
    }

    public long balance() {
        return balance;
    }

    /**
     * Дансны үлдэгдлээс мөнгө гаргах.
     */
    public void withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("insufficient funds: balance=" + balance + ", requested=" + amount);
        }
        balance -= amount;

        assert balance >= 0 : "invariant violated: balance < 0";
    }

    /**
     * Нэг данснаас өөр данс руу мөнгө шилжүүлэх.
     */
    public static void transfer(BankAccount from, BankAccount to, long amount) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("accounts must not be null");
        }
        if (from == to) {
            throw new IllegalArgumentException("cannot transfer to the same account");
        }
        from.withdraw(amount);
        to.deposit(amount);
    }

    /**
     * Дансанд мөнгө нэмэх.
     */
    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        balance += amount;
    }
}
