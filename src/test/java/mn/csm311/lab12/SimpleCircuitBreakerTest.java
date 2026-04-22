package mn.csm311.lab12;

import mn.csm311.lab12.task5.CircuitBreakerOpenException;
import mn.csm311.lab12.task5.SimpleCircuitBreaker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCircuitBreakerTest {

    /** Тестэнд зориулсан хиймэл цаг. */
    static class FakeClock implements SimpleCircuitBreaker.Clock {
        long t = 0;
        @Override public long now() { return t; }
        void advance(long ms) { t += ms; }
    }

    @Test
    void startsInClosedState() {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 1000, new FakeClock());
        assertEquals(SimpleCircuitBreaker.State.CLOSED, cb.state());
    }

    @Test
    void allowsCallsInClosedState() {
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 1000, new FakeClock());
        String result = cb.execute(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void opensAfterThresholdFailures() {
        FakeClock clock = new FakeClock();
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(3, 1000, clock);

        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                    cb.execute(() -> { throw new RuntimeException("boom"); }));
        }
        assertEquals(SimpleCircuitBreaker.State.OPEN, cb.state());
    }

    @Test
    void fastFailsWhenOpen() {
        FakeClock clock = new FakeClock();
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(2, 1000, clock);

        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        // Одоо OPEN
        assertThrows(CircuitBreakerOpenException.class,
                () -> cb.execute(() -> "should-not-run"));
    }

    @Test
    void movesToHalfOpenAfterTimeout() {
        FakeClock clock = new FakeClock();
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(2, 1000, clock);

        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        assertEquals(SimpleCircuitBreaker.State.OPEN, cb.state());

        clock.advance(1001);
        assertEquals(SimpleCircuitBreaker.State.HALF_OPEN, cb.state());
    }

    @Test
    void closesAgainOnSuccessInHalfOpen() {
        FakeClock clock = new FakeClock();
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(2, 1000, clock);

        // Circuit нээгдэнэ
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        clock.advance(1001);

        // HALF_OPEN, амжилттай дуудлага
        String result = cb.execute(() -> "recovered");
        assertEquals("recovered", result);
        assertEquals(SimpleCircuitBreaker.State.CLOSED, cb.state());
    }

    @Test
    void reopensIfHalfOpenCallFails() {
        FakeClock clock = new FakeClock();
        SimpleCircuitBreaker cb = new SimpleCircuitBreaker(2, 1000, clock);

        // Circuit нээгдэнэ
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("boom"); }));
        clock.advance(1001);
        assertEquals(SimpleCircuitBreaker.State.HALF_OPEN, cb.state());

        // HALF_OPEN үед алдаа гармагц буцаад OPEN
        assertThrows(RuntimeException.class, () ->
                cb.execute(() -> { throw new RuntimeException("still-broken"); }));
        assertEquals(SimpleCircuitBreaker.State.OPEN, cb.state());
    }
}
