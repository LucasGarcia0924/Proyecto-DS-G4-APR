import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public class TestHash {
  public static void main(String[] args) throws Exception {
    byte[] salt = Base64.getDecoder().decode("99SxFUiz12283fzBZwYEmw==");
    PBEKeySpec spec = new PBEKeySpec("1234".toCharArray(), salt, 100000, 32*8);
    byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    System.out.println(Base64.getEncoder().encodeToString(hash));
    PBEKeySpec spec2 = new PBEKeySpec("rojo".toCharArray(), salt, 100000, 32*8);
    byte[] hash2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec2).getEncoded();
    System.out.println(Base64.getEncoder().encodeToString(hash2));
  }
}
