package mn.csm311.lab12.task5;

import java.util.function.Supplier;

/**
 * ДААЛГАВАР 5: Энгийн Circuit Breaker.
 *
 * Гурван төлөв:
 *  CLOSED    — ихэнх хүсэлт нэвтэрнэ. Дараалсан N алдаа гармагц → OPEN.
 *  OPEN      — бүх хүсэлтийг шууд CircuitBreakerOpenException-аар татгалзана.
 *              resetTimeoutMs хугацаа өнгөрмөгц → HALF_OPEN.
 *  HALF_OPEN — нэг хүсэлтийг үзэж үзнэ. Амжилттай бол → CLOSED, failureCount=0.
 *              Алдаатай бол → OPEN, тоолуур шинэчлэгдэнэ.
 *
 * Хугацааны мэдрэгчийг (Clock) тестлэх боломжтой болгов. Production-д
 * SystemClock, тестэд FakeClock ашиглана.
 */
public class SimpleCircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    public interface Clock {
        long now();
    }

    public static class SystemClock implements Clock {
        @Override
        public long now() { return System.currentTimeMillis(); }
    }

    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final Clock clock;

    private State state = State.CLOSED;
    private int failureCount = 0;
    private long openedAt = 0L;

    public SimpleCircuitBreaker(int failureThreshold, long resetTimeoutMs) {
        this(failureThreshold, resetTimeoutMs, new SystemClock());
    }

    public SimpleCircuitBreaker(int failureThreshold, long resetTimeoutMs, Clock clock) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold must be >= 1");
        }
        if (resetTimeoutMs < 0) {
            throw new IllegalArgumentException("resetTimeoutMs must be >= 0");
        }
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.clock = clock;
    }

    public State state() {
        // OPEN байх үед тайм-аут өнгөрсөн эсэхийг шалгаж HALF_OPEN руу
        // автоматаар шилжүүлэх тохиромжтой.
        if (state == State.OPEN && clock.now() - openedAt >= resetTimeoutMs) {
            state = State.HALF_OPEN;
        }
        return state;
    }

    /**
     * Үйлчилгээний дуудлагыг circuit breaker-ээр бүрхэнэ.
     *
     * TODO 5.1: Доорх логикийг хэрэгжүүл:
     *
     *   State current = state();  // state() нь OPEN→HALF_OPEN шилжүүлгийг хийнэ
     *
     *   if current == OPEN:
     *       throw new CircuitBreakerOpenException("circuit is open")
     *
     *   try:
     *       T result = op.get()
     *       onSuccess()
     *       return result
     *   catch (RuntimeException e):
     *       onFailure()
     *       throw e
     *
     *
     * TODO 5.2: onSuccess() логик:
     *   - state → CLOSED
     *   - failureCount = 0
     *
     * TODO 5.3: onFailure() логик:
     *   - HALF_OPEN үед алдаа гарсан бол → OPEN, openedAt = clock.now(), failureCount = 0
     *   - CLOSED үед: failureCount++, хэрэв failureCount >= failureThreshold бол
     *       state → OPEN, openedAt = clock.now()
     */
    public <T> T execute(Supplier<T> op) {
        State current = state();
        if (current == State.OPEN) {
            throw new CircuitBreakerOpenException("circuit is open");
        }
        try {
            T result = op.get();
            onSuccess();
            return result;
        } catch (RuntimeException e) {
            onFailure();
            throw e;
        }
    }

    private void onSuccess() {
        state = State.CLOSED;
        failureCount = 0;
    }

    private void onFailure() {
        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            openedAt = clock.now();
            failureCount = 0;
        } else if (state == State.CLOSED) {
            failureCount++;
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
                openedAt = clock.now();
            }
        }
    }
}
