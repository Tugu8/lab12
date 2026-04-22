package mn.csm311.lab12;

import mn.csm311.lab12.task2.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    @Test
    void findByEmail_returnsEmptyWhenMissing() {
        UserRepository repo = new UserRepository();
        Optional<User> result = repo.findByEmail("missing@example.com");
        assertNotNull(result, "Optional<User> нь null байх ёсгүй");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_returnsUserWhenPresent() {
        UserRepository repo = new UserRepository();
        User bat = new User("bat@example.com", "Bat");
        repo.save(bat);

        Optional<User> result = repo.findByEmail("bat@example.com");
        assertTrue(result.isPresent());
        assertEquals(bat, result.get());
    }

    @Test
    void greetingService_returnsNameWhenFound() {
        UserRepository repo = new UserRepository();
        repo.save(new User("bat@example.com", "Bat"));

        GreetingService service = new GreetingService(repo);
        assertEquals("Сайн байна уу, Bat!", service.greet("bat@example.com"));
    }

    @Test
    void greetingService_returnsGuestWhenMissing() {
        UserRepository repo = new UserRepository();
        GreetingService service = new GreetingService(repo);
        assertEquals("Сайн байна уу, Зочин!", service.greet("missing@example.com"));
    }

    @Test
    void nullLogger_doesNothingAndReturnsZero() {
        Logger logger = new NullLogger();
        logger.log("hello");
        logger.log("world");
        assertEquals(0, logger.logCount(),
                "NullLogger нь юу ч log хийхгүй, үргэлж 0 буцаана");
    }

    @Test
    void consoleLogger_incrementsCount() {
        Logger logger = new ConsoleLogger();
        logger.log("first");
        logger.log("second");
        assertEquals(2, logger.logCount());
    }
}
