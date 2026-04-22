# Lab 12 — Designing for Robustness

**Хичээл:** F.CSM311 — Программ хангамжийн бүтээлт  
**Лекц:** 12 — Бат бөх дизайн ба алдаа зохицуулалт  
**Оюутан:** Tugu8

---

## Тестийн үр дүн — Бүх тест ногоон

```
[INFO] Running mn.csm311.lab12.BankAccountTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running mn.csm311.lab12.FileConfigLoaderTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running mn.csm311.lab12.RetryExecutorTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running mn.csm311.lab12.SimpleCircuitBreakerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running mn.csm311.lab12.UserRepositoryTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Даалгавар | Тест тоо | Үр дүн |
|-----------|----------|--------|
| Task 1 — BankAccount (Fail-fast) | 9 / 9 | PASSED |
| Task 2 — Optional & NullLogger | 6 / 6 | PASSED |
| Task 3 — FileConfigLoader (Exception) | 3 / 3 | PASSED |
| Task 4 — RetryExecutor (Backoff+Jitter) | 5 / 5 | PASSED |
| Task 5 — SimpleCircuitBreaker | 7 / 7 | PASSED |
| **Нийт** | **30 / 30** | **BUILD SUCCESS** |

---

## Төслийн бүтэц

```
src/main/java/mn/csm311/lab12/
├── task1/
│   ├── BankAccount.java              — Fail-fast, preconditions, invariant
│   └── InsufficientFundsException.java
├── task2/
│   ├── User.java
│   ├── UserRepository.java           — Optional<User>
│   ├── Logger.java
│   ├── ConsoleLogger.java
│   ├── NullLogger.java               — Null Object Pattern
│   └── GreetingService.java          — Optional API chain
├── task3/
│   ├── AppConfig.java
│   ├── ConfigLoadException.java
│   └── FileConfigLoader.java         — try-with-resources, exception translation
├── task4/
│   ├── NonRetryableException.java
│   ├── FlakyService.java
│   └── RetryExecutor.java            — Exponential backoff + jitter
└── task5/
    ├── CircuitBreakerOpenException.java
    └── SimpleCircuitBreaker.java      — CLOSED / OPEN / HALF_OPEN
```

---

## Даалгавар 1 — Fail-fast ба Preconditions

**Файл:** `src/main/java/mn/csm311/lab12/task1/BankAccount.java`

Оролтын нөхцлийг шалгаж, зөрчигдвөл шууд exception шидэнэ (fail-fast зарчим):

```java
// Constructor
if (owner == null || owner.isBlank())
    throw new IllegalArgumentException("owner must not be blank");
if (initialBalance < 0)
    throw new IllegalArgumentException("initialBalance must be >= 0");

// withdraw
if (amount <= 0)
    throw new IllegalArgumentException("amount must be positive");
if (balance < amount)
    throw new InsufficientFundsException(balance, amount);

// Invariant — хувьсагч зөв байгааг баталгаажуулна
assert balance >= 0 : "balance invariant violated";

// transfer — withdraw амжилттай болсны дараа л deposit
from.withdraw(amount);
to.deposit(amount);
```

**Хамрах тест тохиолдлууд:**
- `null` болон хоосон owner → `IllegalArgumentException`
- Сөрөг эхний үлдэгдэл → `IllegalArgumentException`
- Сөрөг эсвэл тэг дүн зарцуулах → `IllegalArgumentException`
- Хангалтгүй үлдэгдэл → `InsufficientFundsException`
- Өөрөөсөө өөрт шилжүүлэх → `IllegalArgumentException`
- Амжилттай deposit / withdraw / transfer

---

## Даалгавар 2 — Optional ба Null Object Pattern

**Файлууд:** `task2/UserRepository.java`, `task2/NullLogger.java`, `task2/GreetingService.java`

### 2a. Optional\<User\> — null-ийн оронд

```java
public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(byEmail.get(email));
}
```

### 2b. NullLogger — Null Object Pattern

```java
public class NullLogger implements Logger {
    @Override public void log(String message) { }   // юу ч хийхгүй
    @Override public int logCount() { return 0; }   // үргэлж 0
}
```

`if (logger != null)` шалгалтгүйгээр logger-ыг аюулгүй дамжуулна.

### 2c. GreetingService — Optional API гинж

```java
public String greet(String email) {
    return repo.findByEmail(email)
               .map(u -> "Сайн байна уу, " + u.name() + "!")
               .orElse("Сайн байна уу, Зочин!");
}
```

```
greet("bat@example.com")     → "Сайн байна уу, Bat!"
greet("missing@example.com") → "Сайн байна уу, Зочин!"
```

---

## Даалгавар 3 — Exception Handling (FileConfigLoader)

**Файл:** `src/main/java/mn/csm311/lab12/task3/FileConfigLoader.java`

try-with-resources: `BufferedReader` автоматаар хаагдана, exception орчуулалт:

```java
public AppConfig load(Path path) {
    if (path == null)
        throw new IllegalArgumentException("path must not be null");

    Map<String, String> props = new LinkedHashMap<>();
    try (BufferedReader r = Files.newBufferedReader(path)) {
        String line;
        while ((line = r.readLine()) != null)
            parseLine(line, props);
    } catch (NoSuchFileException e) {
        throw new ConfigLoadException("config file not found: " + path, e);
    } catch (IOException e) {
        throw new ConfigLoadException("failed to read config: " + path, e);
    }
    return new AppConfig(props);
}
```

**Шийдсэн 4 асуудал:**
- (A) Exception залгихгүй (swallow хийхгүй)
- (B) `finally` блок дотор `close()` дахин алдаа гарахгүй
- (C) `IOException` дотоод дэлгэрэнгүй гадагш гарахгүй — `ConfigLoadException` болгон орчуулна
- (D) `NoSuchFileException`-ыг ерөнхий `IOException`-оос тусад нь мессежтэй мэдэгдэнэ

---

## Даалгавар 4 — Retry with Exponential Backoff + Jitter

**Файл:** `src/main/java/mn/csm311/lab12/task4/RetryExecutor.java`

```java
public <T> T execute(Supplier<T> op) {
    RuntimeException lastError = null;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            return op.get();
        } catch (NonRetryableException e) {
            throw e;                                       // шууд дамжуулна
        } catch (RuntimeException e) {
            lastError = e;
            if (attempt == maxAttempts) break;             // сүүлчийн → унтахгүй
            long delay = baseDelayMs * (1L << (attempt - 1))
                       + (long)(Math.random() * baseDelayMs);  // exponential + jitter
            try {
                sleeper.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    throw lastError;
}
```

**Delay хүснэгт (baseDelayMs = 100):**

| Оролдлого хооронд | Exponential | + Jitter (max) | Нийт max |
|-------------------|------------|----------------|----------|
| 1 → 2 | 100ms | +100ms | 200ms |
| 2 → 3 | 200ms | +100ms | 300ms |
| 3 → 4 | 400ms | +100ms | 500ms |
| 4 → 5 | 800ms | +100ms | 900ms |

`Sleeper` интерфэйсийг `FakeSleeper`-ээр солих замаар тестүүд бодит унтлагагүй ажиллана.

---

## Даалгавар 5 — Circuit Breaker (3 төлөв)

**Файл:** `src/main/java/mn/csm311/lab12/task5/SimpleCircuitBreaker.java`

```
CLOSED ──(N дараалсан алдаа)──► OPEN ──(resetTimeout өнгөрөх)──► HALF_OPEN
  ▲                                                                    │
  └─────────────────(амжилттай)───────────────────────────────────────┘
                                      (алдаатай)──────────────► OPEN
```

```java
// execute
public <T> T execute(Supplier<T> op) {
    if (state() == State.OPEN)
        throw new CircuitBreakerOpenException();
    try {
        T result = op.get();
        onSuccess();
        return result;
    } catch (Exception e) {
        onFailure();
        throw e;
    }
}

// onSuccess — CLOSED болгон буцаана
void onSuccess() {
    state = State.CLOSED;
    failureCount = 0;
}

// onFailure — төлөвөөс хамааран шилжинэ
void onFailure() {
    if (state == State.HALF_OPEN) {
        state = State.OPEN;
        openedAt = clock.now();
        failureCount = 0;
    } else if (state == State.CLOSED) {
        failureCount++;
        if (failureCount >= failureThreshold)
            state = State.OPEN;
    }
}
```

`Clock` интерфэйсийг `FakeClock`-оор солиж, бодит хугацааны унтлагагүйгээр OPEN → HALF_OPEN шилжилтийг тест хийнэ.

---

## Тест ажиллуулах

```bash
# Бүх тест
mvn test

# Тус бүрчлэн
mvn test -Dtest=BankAccountTest
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=FileConfigLoaderTest
mvn test -Dtest=RetryExecutorTest
mvn test -Dtest=SimpleCircuitBreakerTest
```

---

## Шаардлагатай хэрэгсэл

- Java 17+
- Maven 3.6+

---

*F.CSM311 Lab 12 — Tugu8*
