package mn.csm311.lab12.task2;

public class ConsoleLogger implements Logger {

    private int count = 0;

    @Override
    public void log(String message) {
        System.out.println("[LOG] " + message);
        count++;
    }

    @Override
    public int logCount() {
        return count;
    }
}
