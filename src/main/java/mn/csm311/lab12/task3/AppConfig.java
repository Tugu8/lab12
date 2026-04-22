package mn.csm311.lab12.task3;

import java.util.Map;

public record AppConfig(Map<String, String> properties) {

    public String get(String key) {
        return properties.get(key);
    }
}
