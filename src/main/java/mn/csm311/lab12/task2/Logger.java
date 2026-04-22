package mn.csm311.lab12.task2;

/**
 * Хоёр төрлийн хэрэгжилттэй энгийн log интерфэйс.
 */
public interface Logger {
    void log(String message);

    /**
     * Хэдэн удаа log хийснийг тоолох (тестийн зориулалттай).
     */
    int logCount();
}
