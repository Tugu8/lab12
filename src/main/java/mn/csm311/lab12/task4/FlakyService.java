package mn.csm311.lab12.task4;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Тест зориулалттай "хол зайн" үйлчилгээ.
 * Эхний N-р дуудлагад RuntimeException шидээд, дараа нь амжилттай болно.
 */
public class FlakyService {

    private final int failuresBeforeSuccess;
    private final AtomicInteger callCount = new AtomicInteger();
    private final String successResult;

    public FlakyService(int failuresBeforeSuccess, String successResult) {
        this.failuresBeforeSuccess = failuresBeforeSuccess;
        this.successResult = successResult;
    }

    public String call() {
        int n = callCount.incrementAndGet();
        if (n <= failuresBeforeSuccess) {
            throw new RuntimeException("transient failure #" + n);
        }
        return successResult;
    }

    public int callCount() {
        return callCount.get();
    }

    public Supplier<String> asSupplier() {
        return this::call;
    }
}
