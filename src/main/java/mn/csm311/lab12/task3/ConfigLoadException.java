package mn.csm311.lab12.task3;

/**
 * Тохиргоо уншихад гарсан алдааг дээд түвшний семантик бүхий
 * exception болгон хөрвүүлдэг. Доор IOException, SQLException гэх мэт
 * дотоод хэрэгжилтийн дэлгэрэнгүй API-аас гарахгүй.
 */
public class ConfigLoadException extends RuntimeException {

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
