package mn.csm311.lab12.task5;

/**
 * Circuit breaker OPEN байхад хүсэлт fast-fail болдог.
 */
public class CircuitBreakerOpenException extends RuntimeException {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}
