package modelos;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Scanner;
import com.fasterxml.jackson.databind.ObjectMapper;

import modelos.engine.*;
import vista.consola;
import interfaz.interfaz;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Clase que administra la información del usuario activo.
 */
public class usuario {
    private final Scanner escaner;
    private final consola consola;
    public User usuarioActivo;

    public usuario() {
        this.escaner = new Scanner(System.in);
        this.consola = new consola();
        this.usuarioActivo = null;
    }

    public static class User {
        public String nombreUsuario;
        public String contraseña; // base64
        public String preguntaHash; // base64
        public String respuestaHash; // base64
        public String salt;         // base64
        public engine.Lista<String> equipo = new engine.Lista<>(); // nombres
        public Hash<String, Boolean> owned = new Hash<>();   // nombres
        public Hash<String,Integer> socialLinks = new Hash<>();
        public int mes = 1;
        public String lastModified;
        public User() {}

        public boolean addTeamMember(String nombrePersona) {
            if (nombrePersona == null || nombrePersona.isBlank()) return false;
            if (equipo.contains(nombrePersona)) return false;
            equipo.add(nombrePersona);
            return true;
        }

        public boolean removeTeamMember(String nombrePersona) {
            if (nombrePersona == null || nombrePersona.isBlank()) return false;
            for (int i = 0; i < equipo.size(); i++) {
                String actual = equipo.get(i);
                if (actual != null && actual.equalsIgnoreCase(nombrePersona)) {
                    equipo.remove(actual);
                    return true;
                }
            }
            return false;
        }

        public boolean registerOwned(String nombrePersona) {
            if (nombrePersona == null || nombrePersona.isBlank()) return false;
            owned.put(nombrePersona, true);
            return true;
        }

        public boolean hasOwned(String nombrePersona) {
            if (nombrePersona == null || nombrePersona.isBlank()) return false;
            return owned.containsKey(nombrePersona);
        }

        public int getSocialLinkLevel(String npcName) {
            if (npcName == null || npcName.isBlank()) return 0;
            return socialLinks.getOrDefault(npcName, 0);
        }

        public void setSocialLinkLevel(String npcName, int nivel) {
            if (npcName == null || npcName.isBlank()) return;
            socialLinks.put(npcName, Math.max(0, Math.min(10, nivel)));
        }

        public int increaseSocialLinkLevel(String npcName, int delta) {
            if (npcName == null || npcName.isBlank()) return 0;
            int nuevoNivel = getSocialLinkLevel(npcName) + delta;
            int nivelNormalizado = Math.max(0, Math.min(10, nuevoNivel));
            socialLinks.put(npcName, nivelNormalizado);
            return nivelNormalizado;
        }
}

    public static class PasswordUtil {
        private static final int SALT_BYTES = 16;
        private static final int HASH_BYTES = 32;
        private static final int ITERATIONS = 100_000;

        public static String generateSaltBase64() {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        }

        public static String hashPassword(String password, String saltBase64) throws Exception {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BYTES * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        }

        public static boolean verifyPassword(String password, String saltBase64, String expectedHashBase64) throws Exception {
            String computed = hashPassword(password, saltBase64);
            return MessageDigest.isEqual(Base64.getDecoder().decode(computed), Base64.getDecoder().decode(expectedHashBase64));
        }
    }

    public class managerUsuario {
        private final Path usersDir = Paths.get("Data/users");
        private final ObjectMapper M = new ObjectMapper();
        private final Hash<String, User> users = new Hash<>();

        public managerUsuario() throws IOException {
            if (!Files.exists(usersDir)) Files.createDirectories(usersDir);
            loadAllUsers();
        }

        private void loadAllUsers() throws IOException {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(usersDir, "*.json")) {
                for (Path p : ds) {
                    User u = M.readValue(p.toFile(), User.class);
                    if (u.mes <= 0) u.mes = 1;
                    if (u.socialLinks == null) u.socialLinks = new Hash<>();
                    users.put(u.nombreUsuario.toLowerCase(), u);
                }
            }
        }

        public boolean usernameExists(String username) { return users.containsKey(username.toLowerCase()); }

        public User authenticate(String username, String password) throws Exception {
            User u = users.get(username.toLowerCase());
            if (u == null) return null;
            if (PasswordUtil.verifyPassword(password, u.salt, u.contraseña)) return u;
            return null;
        }

        public User recobrarContraseña() throws Exception {
            System.out.print("Ingresa tu nombre de usuario: ");
            String username = escaner.nextLine();

            User u = getUser(username);
            if (u == null) {
                System.out.println("Usuario no encontrado.");
                return null;
            }

            System.out.println("Pregunta de seguridad: " + u.preguntaHash);
            System.out.print("Ingresa tu respuesta: ");
            String respuesta = escaner.nextLine();

            if (PasswordUtil.verifyPassword(respuesta, u.salt, u.respuestaHash)) {
                System.out.print("Ingresa tu nueva contraseña: ");
                String nuevaContraseña = escaner.nextLine();
                String nuevaHash = PasswordUtil.hashPassword(nuevaContraseña, u.salt);
                u.contraseña = nuevaHash;
                saveUser(u);
                return u;
            }

            return null;
        }

        public synchronized void saveUser(User u) throws IOException {
            u.lastModified = Instant.now().toString();
            Path out = usersDir.resolve(u.nombreUsuario + ".json");
            M.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), u);
        }

        public User getUser(String username) { return users.get(username.toLowerCase()); }

        public User getUsuarioActivo() { return usuarioActivo; }

        public void setUsuarioActivo(User usuarioActivo) {usuario.this.usuarioActivo = usuarioActivo; }

    }

    public class UserView {
        public final User usuario;
        public final engine.Registro registro;
        public final engine.indiceFusiones indiceF;
        public final engine.grafoSocialLinks grafoS;
        public final engine.indicePorNivel indiceN;
        public final engine.Equipo equipo;

        public UserView(User user, engine.Registro registro, engine.indiceFusiones fusionIndex, engine.grafoSocialLinks socialGraph, engine.indicePorNivel levelIndex, int teamCapacity) {
            this.usuario = user;
            this.registro = registro;
            this.indiceF = fusionIndex;
            this.grafoS = socialGraph;
            this.indiceN = levelIndex;
            this.equipo = new engine.Equipo(teamCapacity);
            resolveTeamFromUser();
            grafoS.ensureNPCsFor(usuario.socialLinks.keySet());
            grafoS.syncLevels(usuario.socialLinks);
        }

        // Resuelve nombres del usuario a referencias Persona y llena Team si hay espacio
        private void resolveTeamFromUser() {
            for (String name : usuario.equipo) {
                Persona p = registro.buscarPorNombre(name);
                if (p != null) equipo.agregarPersona(p);
            }
        }

        public engine.Lista<Persona> getEquipoComoPersonas() {
            engine.Lista<Persona> miembros = new engine.Lista<>();
            for (String name : usuario.equipo) {
                Persona p = registro.buscarPorNombre(name);
                if (p != null) miembros.add(p);
            }
            return miembros;
        }

        public void mostrarEquipoDetallado() {
            System.out.println("Equipo actual:");
            engine.Lista<Persona> miembros = getEquipoComoPersonas();
            if (miembros.isEmpty()) {
                System.out.println("<vacío>");
                return;
            }

            for (Persona p : miembros) {
                System.out.println("- " + p.nombre + " | Arcano: " + p.arcano + " | Nivel: " + p.nivel);
            }
        }

        public engine.Equipo buildTeamFromUser(int capacity) {
            engine.Equipo runtimeTeam = new engine.Equipo(capacity);
            for (String name : usuario.equipo) {
                Persona p = registro.buscarPorNombre(name);
                if (p != null) runtimeTeam.agregarPersona(p);
            }
            return runtimeTeam;
        }

        public void syncTeamFromRuntime(engine.Equipo runtimeTeam) {
            usuario.equipo.clear();
            for (Persona p : runtimeTeam.getMiembros()) {
                if (p != null) usuario.equipo.add(p.nombre);
            }
        }

        public boolean releaseFromTeam(String nombrePersona, managerUsuario um) throws IOException {
            boolean removed = equipo.liberarPersona(nombrePersona);
            if (removed) {
                for (int i = 0; i < usuario.equipo.size(); i++) {
                    String actual = usuario.equipo.get(i);
                    if (actual != null && actual.equalsIgnoreCase(nombrePersona)) {
                        usuario.equipo.remove(actual);
                        break;
                    }
                }
                um.saveUser(usuario);
                return true;
            }
            return false;
        }

        public boolean autoRegister(String nombrePersona, managerUsuario um) throws IOException {
            if (usuario.hasOwned(nombrePersona)){
                return false;
            }
            usuario.registerOwned(nombrePersona);
            um.saveUser(usuario);
            return true;
        }

        public boolean isOnTeam(String nombrePersona, managerUsuario um) throws IOException {
            if (nombrePersona == null || nombrePersona.isBlank()) return false;
            for (Persona p : equipo.getMiembros()) {
                if (p == null) continue;
                if (p.nombre != null && p.nombre.equalsIgnoreCase(nombrePersona)) return true;
            }
            return false;
        }

        public boolean addToTeam(String nombrePersona, managerUsuario um) throws IOException {
            if (!equipo.tieneEspacio()){
                System.out.print("El equipo está lleno.");
                return false;
            }
            if (isOnTeam(nombrePersona, um)){
                System.out.println("Persona ya en el equipo.");

            }
            Persona p = registro.buscarPorNombre(nombrePersona);
            if (p == null) {
                System.out.print("Nombre incorrecto, no se halló la Persona.");
                return false;
            }
            boolean ok = equipo.agregarPersona(p);
            if (ok) {
                usuario.addTeamMember(nombrePersona);
                um.saveUser(usuario);
                return true;
            }
            return false;
        }


        public void printRegistryWithOwned() {
            System.out.println("Registro (owned marcado):");
            for (Persona p : registro.toList()) {
                System.out.println((usuario.hasOwned(p.nombre) ? "[X] " : "[ ] ") + p);
            }
        }

        public void printRegistryByArcana() {
            System.out.println("Registro por arcano:");
            Hash<String, engine.Lista<Persona>> porArcano = new Hash<>();
            for (Persona p : registro.toList()) {
                engine.Lista<Persona> bucket = porArcano.get(p.arcano);
                if (bucket == null) {
                    bucket = new engine.Lista<>();
                    porArcano.put(p.arcano, bucket);
                }
                bucket.add(p);
            }

            engine.Lista<String> arcanos = porArcano.keySet();
            arcanos.sort();

            for (int i = 0; i < arcanos.size(); i++) {
                String arcano = arcanos.get(i);
                System.out.println("\n[" + arcano + "]");
                engine.Lista<Persona> personasArcano = porArcano.get(arcano);
                for (int j = 0; j < personasArcano.size(); j++) {
                    Persona p = personasArcano.get(j);
                    System.out.println((usuario.hasOwned(p.nombre) ? "[X] " : "[ ] ") + p.nombre + " | Nivel: " + p.nivel);
                }
            }
        }

        public boolean fuseAndReplace(String nameA, String nameB, managerUsuario um) throws IOException {
            Persona a = registro.buscarPorNombre(nameA);
            Persona b = registro.buscarPorNombre(nameB);
            if (a == null || b == null) return false;

            String resultName = indiceF.resultadoFusion(a, b);
            if (resultName == null || resultName.isBlank()) return false; // no hay fusión conocida

            Persona resultPersona = registro.buscarPorNombre(resultName);
            if (resultPersona == null) {
                // Si el resultado no existe en el registro maestro, no podemos crear uno nuevo aquí.
                return false;
            }
            if (!grafoS.isFusionUnlocked(resultPersona, usuario)) {
                String requiredNpc = resultPersona.requisitoFusion == null ? "?" : resultPersona.requisitoFusion.socialLink;
                int nivelRequerido = grafoS.getRequiredLevel(requiredNpc);
                System.out.println("No puedes fusionar estas personas todavía. Requiere Social Link '" + requiredNpc + "' nivel " + nivelRequerido + ".");
                return false;
            }
            if (isOnTeam(resultPersona.nombre, um)){
                System.out.println("La persona obtenida ya se encuentra en el equipo, fusión inválida.\n");
                return false;
            }

            // 5) Si usuario no tiene registrado el resultado, marcarlo automáticamente
            if (!usuario.hasOwned(resultName)) {
                usuario.registerOwned(resultName);
            }

            // 6) Remover las dos personas del equipo y añadir el resultado (si hay espacio)
            equipo.removerPersona(a);
            equipo.removerPersona(b);
            // también actualizar user.team (nombres)
            usuario.removeTeamMember(a.nombre);
            usuario.removeTeamMember(b.nombre);
            boolean added = equipo.agregarPersona(resultPersona);
            if (added) usuario.addTeamMember(resultName);
            // persistir cambios del usuario
            um.saveUser(usuario);
            return true;
        }

        // Incrementar social link y auto registrar desbloqueos
        public String increaseSocialLinkAndHandleUnlock(String npcName, int delta, managerUsuario um) throws IOException {
            if (npcName == null || npcName.isBlank()) return null;
            grafoS.ensureNPC(npcName);
            int nuevoNivel = usuario.increaseSocialLinkLevel(npcName, delta);
            String unlocked = grafoS.aumentarRango(npcName, delta, usuario.mes, usuario.socialLinks);
            if (unlocked != null && !usuario.hasOwned(unlocked)) {
                usuario.registerOwned(unlocked);
            }
            um.saveUser(usuario);
            return unlocked;
        }
    }
    public void seleccionarUsuario() throws Exception {
        consola.menuUsuarios();

        boolean flag = true;
        while (flag == true) {
            String opcion = escaner.nextLine();
            switch (opcion) {
                case "1" : {
                    iniciarSesion();
                    flag = false;
                    break;
                }
                case "2" : {
                    interfaz interfaz = new interfaz();
                    interfaz.crearUsuario();
                    System.out.println("\nAhora por favor vuelve a ingresar los datos para iniciar sesión");
                    iniciarSesion();
                    flag = false;    
                    break;
                }
                case "3" : {
                // Se rompe el bucle de selección pero no se continua el proceso de cambio de usuario
                    flag = false;    
                    break;
                }
                default:
                     System.out.println("Opción no válida. Por favor, seleccione '1' o '2'.");
                break;     
            }
            // Se rompe el bucle de selección y se continua el proceso de cambio de usuario
                    }
    }
    public void iniciarSesion() throws Exception {
        managerUsuario mU = new managerUsuario();
        System.out.print("Ingresa tu nombre de usuario: ");
        String nombreUsuario = escaner.nextLine();
        System.out.print("Ingresa tu contraseña: ");
        String contraseña = escaner.nextLine();

        User user = mU.authenticate(nombreUsuario, contraseña);
        if (user != null) {
            System.out.println("Inicio de sesión exitoso. ¡Bienvenido, " + user.nombreUsuario + "!");
            // Aquí puedes continuar con la lógica del programa después de un inicio de sesión exitoso
            mU.setUsuarioActivo(user);

        } else {
            for (int i = 0; i < 3; i++){
                System.out.println("Nombre de usuario o contraseña incorrectos. Intenta nuevamente.");
                System.out.print("Ingresa tu nombre de usuario: ");
                nombreUsuario = escaner.nextLine();
                System.out.print("Ingresa tu contraseña: ");
                contraseña = escaner.nextLine();
                user = mU.authenticate(nombreUsuario, contraseña);
                if (user != null) {
                    System.out.println("Inicio de sesión exitoso. ¡Bienvenido, " + user.nombreUsuario + "!");
                    mU.setUsuarioActivo(user);
                    return; // Salir del método después de un inicio de sesión exitoso
                }
            }
            System.out.println("Has excedido el número máximo de intentos.");
            System.out.print("¿Deseas recuperar tu contraseña? (s/n): ");
            String opcion = escaner.nextLine();

            while (!opcion.equalsIgnoreCase("s") && !opcion.equalsIgnoreCase("n")) {
                System.out.println("Opción no válida. Por favor, selecciona 's' o 'n'.");
                System.out.print("¿Deseas recuperar tu contraseña? (s/n): ");
                opcion = escaner.nextLine();
            }

            if (opcion.equalsIgnoreCase("s")) {
                user = mU.recobrarContraseña();
                if (user != null) {
                    iniciarSesion(); // Volver a iniciar sesión después de actualizar la contraseña
                    return; // Salir del método después de actualizar la contraseña
                }
                System.out.println("Respuesta incorrecta. No se pudo recuperar la contraseña.");
            } else {
                System.out.println("Regresando al menú principal.");
            }

            System.out.println("Nombre de usuario o contraseña incorrectos. Intenta nuevamente.");
            seleccionarUsuario(); // Volver a la selección de usuario
        }
    }
}