package mn.csm311.lab12.task2;

/**
 * Энэ класс нь Optional API-г зохистой ашиглаж байгааг харуулна.
 * Оюутны бичсэн UserRepository ажиллах ёстой.
 */
public class GreetingService {

    private final UserRepository repository;

    public GreetingService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * ДААЛГАВАР 2c: Энэ аргыг Optional ашиглан орон зайгаа эзэмшигч
     * байдлаар бич.
     *
     * Хэрэглэгч email-ээр олдсон бол "Сайн байна уу, {name}!" буцаана.
     * Олдоогүй бол "Сайн байна уу, Зочин!" буцаана.
     *
     * Санамж: `repository.findByEmail(email)` нь Optional<User> буцаана
     * (Даалгавар 2a-г хийсний дараа). `.map(...)`, `.orElse(...)` гэсэн
     * хоёр гинжтэй дуудлагаар гоё шийдэл гарна.
     */
    public String greet(String email) {
        return repository.findByEmail(email)
                .map(user -> "Сайн байна уу, " + user.name() + "!")
                .orElse("Сайн байна уу, Зочин!");
    }
}
