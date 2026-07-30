import com.fasterxml.jackson.databind.ObjectMapper;
import modelos.usuario;

public class UsuarioJsonTest {
    public static void main(String[] args) throws Exception {
        String json = """
        {
          "nombreUsuario": "test",
          "equipo": ["Orpheus"],
          "owned": [],
          "socialLinks": {"Akihiko": 0, "Shinjiro": 3}
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        usuario.User user = mapper.readValue(json, usuario.User.class);

        if (user.equipo.size() != 1 || user.equipo.get(0) == null || !user.equipo.get(0).equals("Orpheus")) {
            throw new AssertionError("La lista del equipo no se cargó correctamente");
        }

        if (user.owned.size() != 0) {
            throw new AssertionError("El hash de owned debería quedar vacío");
        }

        if (user.socialLinks.get("Akihiko") != 0 || user.socialLinks.get("Shinjiro") != 3) {
            throw new AssertionError("Los social links no se cargaron correctamente");
        }

        System.out.println("Prueba de deserialización OK");
    }
}
