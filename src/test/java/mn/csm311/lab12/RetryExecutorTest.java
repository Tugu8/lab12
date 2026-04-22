package mn.csm311.lab12;

import mn.csm311.lab12.task4.FlakyService;
import mn.csm311.lab12.task4.NonRetryableException;
import mn.csm311.lab12.task4.RetryExecutor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetryExecutorTest {

    /**
     * Тестлэхэд зориулсан sleeper — бодитоор унтдаггүй,
     * гагцхүү дуудагдсан хугацаануудыг бүртгэнэ.
     */
    static class RecordingSleeper implements RetryExecutor.Sleeper {
        final List<Long> delays = new ArrayList<>();

        @Override
        public void sleep(long millis) {
            delays.add(millis);
        }
    }

    @Test
    void succeedsImmediatelyOnFirstTry() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryExecutor retry = new RetryExecutor(3, 100, sleeper);

        String result = retry.execute(() -> "ok");
        assertEquals("ok", result);
        assertTrue(sleeper.delays.isEmpty(),
                "Амжилттай болбол хүлээх шаардлагагүй");
    }

    @Test
    void retriesUntilSuccess() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryExecutor retry = new RetryExecutor(5, 100, sleeper);
        FlakyService service = new FlakyService(3, "finally-ok");

        String result = retry.execute(service.asSupplier());

        assertEquals("finally-ok", result);
        assertEquals(4, service.callCount(),
                "3 удаа алдаа + 1 амжилт = 4 дуудлага");
        assertEquals(3, sleeper.delays.size(),
                "3 алдааны хооронд 3 удаа унтана");
    }

    @Test
    void delaysGrowExponentially() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryExecutor retry = new RetryExecutor(4, 100, sleeper);
        FlakyService service = new FlakyService(3, "ok");

        retry.execute(service.asSupplier());

        // Jitter байгаа тул хатуу тэнцүү биш, муж байдлаар шалгана.
        // attempt 1 дараа: 100 * 2^0 = 100, + jitter [0,100] → [100, 200]
        // attempt 2 дараа: 100 * 2^1 = 200, + jitter [0,100] → [200, 300]
        // attempt 3 дараа: 100 * 2^2 = 400, + jitter [0,100] → [400, 500]
        assertEquals(3, sleeper.delays.size());
        assertTrue(sleeper.delays.get(0) >= 100 && sleeper.delays.get(0) <= 200,
                "1-р хүлээлт " + sleeper.delays.get(0));
        assertTrue(sleeper.delays.get(1) >= 200 && sleeper.delays.get(1) <= 300,
                "2-р хүлээлт " + sleeper.delays.get(1));
        assertTrue(sleeper.delays.get(2) >= 400 && sleeper.delays.get(2) <= 500,
                "3-р хүлээлт " + sleeper.delays.get(2));
    }

    @Test
    void throwsAfterMaxAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryExecutor retry = new RetryExecutor(3, 10, sleeper);
        FlakyService alwaysFails = new FlakyService(100, "never");

        assertThrows(RuntimeException.class,
                () -> retry.execute(alwaysFails.asSupplier()));

        assertEquals(3, alwaysFails.callCount());
        // Сүүлийн оролдлогын дараа унтдаггүй — 3 оролдлого → 2 хүлээлт
        assertEquals(2, sleeper.delays.size());
    }

    @Test
    void doesNotRetryNonRetryableException() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryExecutor retry = new RetryExecutor(5, 100, sleeper);

        assertThrows(NonRetryableException.class, () ->
                retry.execute(() -> {
                    throw new NonRetryableException("400 bad request");
                }));

        assertTrue(sleeper.delays.isEmpty(),
                "NonRetryable тохиолдолд дахин оролдохгүй");
    }
}
