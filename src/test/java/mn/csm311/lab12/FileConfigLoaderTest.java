package mn.csm311.lab12;

import mn.csm311.lab12.task3.AppConfig;
import mn.csm311.lab12.task3.ConfigLoadException;
import mn.csm311.lab12.task3.FileConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileConfigLoaderTest {

    @Test
    void loadsValidConfigFile(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("app.properties");
        Files.writeString(file, """
                # application config
                host=localhost
                port=8080
                timeout=5000
                """);

        FileConfigLoader loader = new FileConfigLoader();
        AppConfig config = loader.load(file);

        assertEquals("localhost", config.get("host"));
        assertEquals("8080", config.get("port"));
        assertEquals("5000", config.get("timeout"));
    }

    @Test
    void throwsConfigLoadExceptionWhenFileMissing(@TempDir Path tmp) {
        FileConfigLoader loader = new FileConfigLoader();
        Path missing = tmp.resolve("does-not-exist.properties");

        ConfigLoadException ex = assertThrows(
                ConfigLoadException.class,
                () -> loader.load(missing));
        assertTrue(ex.getMessage().contains("not found"),
                "Мессеж нь 'not found' гэсэн үгтэй байх ёстой");
        assertNotNull(ex.getCause(), "cause алдалгүй дамжуулагдсан байх ёстой");
    }

    @Test
    void throwsIllegalArgumentWhenPathIsNull() {
        FileConfigLoader loader = new FileConfigLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.load(null));
    }
}
