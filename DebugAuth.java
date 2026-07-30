import modelos.usuario;

public class DebugAuth {
    public static void main(String[] args) throws Exception {
        usuario u = new usuario(new java.util.Scanner(System.in));
        usuario.managerUsuario m = u.new managerUsuario();
        usuario.User user = m.getUser("prueba");
        System.out.println("loaded user: " + (user == null ? "null" : user.nombreUsuario));
        if (user != null) {
            System.out.println("storedPassword='" + user.contraseña + "'");
            System.out.println("storedSalt='" + user.salt + "'");
            System.out.println("authenticate 1234=" + m.authenticate("prueba", "1234"));
            System.out.println("authenticate 1230=" + m.authenticate("prueba", "1230"));
            System.out.println("authenticate    1234    =" + m.authenticate("prueba", "   1234   "));
            System.out.println("authenticate 123=" + m.authenticate("prueba", "123"));
        }
    }
}
 
