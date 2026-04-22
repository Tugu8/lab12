package mn.csm311.lab12.task2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ДААЛГАВАР 2a: Энэ repository нь null-аар "олдоогүй" гэж илэрхийлдэг.
 *  - Дуудлагчид null-check хийх үүрэг үлгэгдэнэ.
 *  - NullPointerException-ийн эх үүсвэр олон болно.
 *
 * Таны даалгавар: findByEmail-ыг Optional<User>-ээс буцаадаг болгож
 * өөрчлөх. Тип систем нь "энэ байхгүй байж магадгүй" гэдгийг албадан
 * сануулна.
 */
public class UserRepository {

    private final Map<String, User> byEmail = new HashMap<>();

    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        byEmail.put(user.email(), user);
    }

    /**
     * Email-ээр хэрэглэгчийг олох.
     *
     * TODO 2.1: Буцах төрлийг User-ээс Optional<User> болгож өөрчлөх.
     *           null-ийг Optional.empty() болгож, байгаа бол
     *           Optional.of(...) хэлбэрээр буцаах.
     *           Заавар: Optional.ofNullable(...) нь энэ хоёрыг аль алиныг
     *           нэг дор хийнэ.
     */
    public Optional<User> findByEmail(String email) {
        // TODO 2.1: өөрчлөх
        return Optional.ofNullable(byEmail.get(email));
    }
}
