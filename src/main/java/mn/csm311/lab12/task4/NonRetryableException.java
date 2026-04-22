package mn.csm311.lab12.task4;

/**
 * Дахин оролдох ёсгүй алдааг илэрхийлэх (жишээ: 4xx client error,
 * authentication error).
 */
public class NonRetryableException extends RuntimeException {
    public NonRetryableException(String message) {
        super(message);
    }
}
