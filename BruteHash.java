import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class BruteHash {
  public static void main(String[] args) throws Exception {
    byte[] salt = Base64.getDecoder().decode("99SxFUiz12283fzBZwYEmw==");
    String[] candidates = {"1234", "12345", "123456", "password", "admin", "prueba", "rojo", "color", "Color", "ROJO", "rojo123", "azul", "verde", "amarillo", "morado", "negro", "blanco", "amarillo", "rojo1234", "123"};
    for (String pw : candidates) {
      PBEKeySpec spec = new PBEKeySpec(pw.toCharArray(), salt, 100000, 256);
      byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
      System.out.println(pw + " -> " + Base64.getEncoder().encodeToString(hash));
    }
  }
}
