package mn.csm311.lab12.task1;

/**
 * Дансны үлдэгдэл хангалтгүй үед шидэгдэх тусгай exception.
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
