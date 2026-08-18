import org.springframework.security.crypto.bcrypt.BCrypt;
public class HashGen {
    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("admin123", BCrypt.gensalt(10)));
        System.out.println(BCrypt.hashpw("writer123", BCrypt.gensalt(10)));
        System.out.println(BCrypt.hashpw("reader123", BCrypt.gensalt(10)));
    }
}
