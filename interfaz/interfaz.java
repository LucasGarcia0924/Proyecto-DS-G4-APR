package interfaz;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.fasterxml.jackson.databind.ObjectMapper;

import modelos.engine.Persona;
import modelos.usuario;
import modelos.usuario.PasswordUtil;
import vista.consola;

/**
 * Clase orquestadora principal del asistente de fusiones.
 * Gestiona el flujo completo de la interfaz con la que interactua el usuario.
 */
public class interfaz {
    private final modelos.usuario usuario;
    private final vista.consola consola;
    private boolean finPrograma;
    private final Scanner escaner;

    public interfaz() {
        this.consola = new consola();
        this.finPrograma = false;
        this.usuario = new usuario();
        this.escaner = new Scanner(System.in);
    }

    /**
     * Inicia el juego.
     */
    public void iniciar() {
        
        consola.mostrarBienvenida();

        try {
            usuario.seleccionarUsuario();
        } catch (Exception e) {
            System.out.println("Error al seleccionar usuario: " + e.getMessage());
        }
        
        // 2. Mostrar menú principal
        try {
            while (!finPrograma) {
            consola.mostrarMenuPrincipal();
            String opcion = escaner.nextLine();
            switch (opcion) {
                // Lógica para llamar a las funciones que se encargan de cada opción del menú
                case "1":
                    verEquipo();
                        break;
                case "2":
                    verRegistro();
                        break;
                case "3":
                    verSocialLinks();
                        break;
                case "4":
                    buscarPersona();
                        break;
                case "5":
                    verFusionesEspeciales();
                        break;
                case "6":
                    cambiarUsuario();
                        break;
                case "7":
                    finPrograma = true;
                    System.out.println("Éxitos en el juego. ¡Hasta la próxima!");
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, seleccione una opción del 1 al 7.");
            }
        }
        } catch (Exception e) {
            System.out.println("Error durante la ejecución del programa: " + e.getMessage());
        }
    }
    public synchronized void crearUsuario() throws Exception {
        usuario.managerUsuario mU = usuario.new managerUsuario();

        System.out.print("Ingresa tu nombre de usuario: ");
        String nombreUsuario = escaner.nextLine();
        System.out.print("Ingresa contraseña: ");
        String contraseña = escaner.nextLine();
        System.out.print("Ingresa pregunta de seguridad: ");
        String pregunta = escaner.nextLine();
        System.out.print("Ingresa respuesta: ");
        String respuesta = escaner.nextLine();

        if (mU.usernameExists(nombreUsuario)) throw new IllegalArgumentException("Usuario ya existe");
        String salt = PasswordUtil.generateSaltBase64();
        String hash = PasswordUtil.hashPassword(contraseña, salt);
        String respuestaHash = PasswordUtil.hashPassword(respuesta, salt);

        usuario.User tu = new modelos.usuario.User();
        tu.nombreUsuario = nombreUsuario;
        tu.salt = salt;
        tu.contraseña = hash;
        tu.preguntaHash = pregunta;
        tu.respuestaHash = respuestaHash;
        tu.equipo = new ArrayList<>(Arrays.asList("Orpheus"));
        tu.owned = new HashSet<>();
        tu.registerOwned("Orpheus");
        tu.socialLinks = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Path socialLinksPath = Paths.get("Data/socialLinks.json");
        if (Files.exists(socialLinksPath)) {
            modelos.engine.SocialLinkData slData = mapper.readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
            if (slData != null && slData.socialLinks != null) {
                for (modelos.engine.SocialLinkEntry entry : slData.socialLinks) {
                    if (entry != null && entry.npc != null && !entry.npc.isBlank()) {
                        tu.socialLinks.put(entry.npc, 0);
                    }
                }
            }
        } else {
            for (String npc : Arrays.asList("S.E.E.S.","Kenji Tomochika","Fuuka Yamagishi",
            "Mitsuru Kirijo","Hidetoshi Odagiri","Bunkichi y Mitsuko","Yukari Takeba",
            "Kazushi Miyamoto", "Chihiro Fushimi", "Maya", "Keisuke Hiraga","Yuko Nishiwaki",
            "Maiko Oohashi","Pharos","Bebe","Presidente Tanaka", "Mutatsu","Mamoru Hayase",
            "Nozomi Suemitsu", "Nozomi Suemitsu","Akinari Kamiki","Equipo Aniquilación de Nyx", "Aigis",
            "Junpei Iori", "Akihiko Sanada", "Ken Amada", "Koromaru", "Shinjiro Aragaki", "Ryoji Mochizuki")) {
                tu.socialLinks.put(npc, 0);
            }
        }
        tu.lastModified = Instant.now().toString();

        mU.saveUser(tu);
        System.out.println("Usuario creado exitosamente.");
    }
    public void verEquipo() throws Exception {
        usuario.managerUsuario mU = usuario.new managerUsuario();
        usuario.User tu = mU.getUsuarioActivo();

        if (tu == null) {
            System.out.println("No hay usuario activo. Por favor, inicia sesión o crea un usuario.");
            return;
        }

        Path personasDir = Paths.get("Data/personas");
        ObjectMapper mapper = new ObjectMapper();
        List<modelos.engine.Persona> personas = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(personasDir, "*.json")) {
            for (Path p : ds) {
                modelos.engine.Persona persona = mapper.readValue(p.toFile(), modelos.engine.Persona.class);
                personas.add(persona);
            }
        }

        modelos.engine.Registro registro = new modelos.engine.Registro();
        registro.buildFrom(personas);

        modelos.engine.indiceFusiones indiceF = new modelos.engine.indiceFusiones();
        modelos.engine.grafoSocialLinks socialGraph = new modelos.engine.grafoSocialLinks();
        modelos.engine.indicePorNivel levelIndex = new modelos.engine.indicePorNivel();
        indiceF.construirDe(personas);
        levelIndex.buildFrom(personas);
        Path socialLinksPath = Paths.get("Data/socialLinks.json");
        if (Files.exists(socialLinksPath)) {
            modelos.engine.SocialLinkData slData = mapper.readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
            socialGraph.construirDesdeSocialLinks(slData.socialLinks);
        } else {
            socialGraph.construirDesdePersonas(personas);
        }

        usuario.UserView userView = new usuario().new UserView(tu, registro, indiceF, socialGraph, levelIndex, 8);

        boolean submenu = true;
        while (submenu) {
            System.out.println("Usuario activo: " + tu.nombreUsuario);
            userView.mostrarEquipoDetallado();
            consola.mostrarMenuEquipo();
            System.out.print("Opción: ");
            String opcion1 = escaner.nextLine();

            switch (opcion1) {
                case "1":
                    int cantidadMiembros = userView.equipo.espaciosOcupados();
                    System.out.println("\nFusiones disponibles con el equipo actual:\n");
                    for (int i = 0; i < cantidadMiembros; i++){
                        for (int j = 0; j < cantidadMiembros; j++){
                            Persona a = userView.equipo.miembros[i];
                            Persona b = userView.equipo.miembros[j];
                            if (a != b){
                                String resultado = userView.indiceF.resultadoFusion(a, b);
                                if (resultado == null) {continue;}
                                System.out.println("Resultado: " + resultado + ", a partir de: " + a.nombre + " y " + b.nombre);
                            }
                        }
                    }
                    
                    boolean bandera2 = false;
                    boolean flag = true;
                    while (flag == true) {
                        System.out.print("\n¿Desea realizar una fusión? (s/n): ");
                        String opcion2 = escaner.nextLine();
                        switch (opcion2){
                            case "s" : {
                                flag = false;
                                bandera2 = true;
                                break;
                            }
                            case "n" : {
                            // Se rompe el bucle de selección pero no se continua el proceso de cambio de usuario
                                flag = false;    
                                break;
                            }
                            default:
                                System.out.println("Opción no válida. Por favor, seleccione 's' o 'n'.");
                            break;     
                        }
                    }
                    boolean bandera = false;
                    String nombre1 = "";
                    String nombre2 = "";

                    if (bandera2 == true){
                        while (!bandera) {
                            System.out.println("\nIngresa el nombre de la primera persona a fusionar");
                            nombre1 = escaner.nextLine();
                            if (userView.isOnTeam(nombre1, mU)) {
                                bandera = true;
                                System.out.println("Persona seleccionada adecuadamente");
                            } else {
                                System.out.println("La Persona no se encuentra en el equipo, intenta denuevo.");
                            }
                        }
                        bandera = false;
                        while (!bandera) {
                            System.out.println("\nIngresa el nombre de la siguiente persona a fusionar");
                            nombre2 = escaner.nextLine();
                            if (!userView.isOnTeam(nombre2, mU)) {
                                System.out.println("La persona no está en el equipo, intenta denuevo.");
                            } else if (nombre2.equals(nombre1)) {
                                System.out.println("No puedes fusionar una persona consigo misma.");
                                } else {
                                    userView.fuseAndReplace(nombre1, nombre2, mU);
                                    bandera = true;
                                }
                            }
                        }
                    break;
            
                case "2":
                    System.out.print("Ingresa el nombre de la persona que quieres agregar al equipo: ");
                    String nombre = escaner.nextLine();
                    if (userView.addToTeam(nombre, mU)) {
                        System.out.println("Persona añadida al equipo.");
                    } else {
                        System.out.println(" No se pudo agregar.");
                    }
                    break;
                case "3":
                    System.out.print("Ingresa el nombre de la persona a liberar: ");
                    String liberar = escaner.nextLine();
                    if (userView.releaseFromTeam(liberar, mU)) {
                        System.out.println("Persona liberada del equipo.");
                    } else {
                        System.out.println("No se encontró esa persona en el equipo.");
                    }
                    break;
                case "4":
                    submenu = false;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    public void verRegistro() throws Exception {
        usuario.managerUsuario mU = usuario.new managerUsuario();
        usuario.User tu = mU.getUsuarioActivo();

        if (tu == null) {
            System.out.println("No hay usuario activo. Por favor, inicia sesión o crea un usuario.");
            return;
        }

        Path personasDir = Paths.get("Data/personas");
        ObjectMapper mapper = new ObjectMapper();
        List<modelos.engine.Persona> personas = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(personasDir, "*.json")) {
            for (Path p : ds) {
                modelos.engine.Persona persona = mapper.readValue(p.toFile(), modelos.engine.Persona.class);
                personas.add(persona);
            }
        }

        modelos.engine.Registro registro = new modelos.engine.Registro();
        registro.buildFrom(personas);

        modelos.engine.indiceFusiones indiceF = new modelos.engine.indiceFusiones();
        modelos.engine.grafoSocialLinks socialGraph = new modelos.engine.grafoSocialLinks();
        modelos.engine.indicePorNivel levelIndex = new modelos.engine.indicePorNivel();
        indiceF.construirDe(personas);
        levelIndex.buildFrom(personas);
        Path socialLinksPath = Paths.get("Data/socialLinks.json");
        if (Files.exists(socialLinksPath)) {
            modelos.engine.SocialLinkData slData = mapper.readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
            socialGraph.construirDesdeSocialLinks(slData.socialLinks);
        } else {
            socialGraph.construirDesdePersonas(personas);
        }

        usuario.UserView userView = new usuario().new UserView(tu, registro, indiceF, socialGraph, levelIndex, 12);

        boolean submenu = true;
        while (submenu) {
            consola.mostrarMenuRegistro();
            System.out.print("Opción: ");
            String opcion = escaner.nextLine();

            switch (opcion) {
                case "1":
                    userView.printRegistryWithOwned();
                    break;
                case "2":
                    userView.printRegistryByArcana();
                    break;
                case "3":
                    System.out.print("Nombre: ");
                    String nombre = escaner.nextLine();

                    modelos.engine.Persona nueva = registro.buscarPorNombre(nombre);
                    if (nueva == null) {
                        System.out.println("No se encontró una persona con ese nombre en el registro maestro.");
                        break;
                    }

                    tu.registerOwned(nombre);
                    mU.saveUser(tu);
                    System.out.println("Persona registrada en el usuario. Arcano: " + nueva.arcano + ", Nivel: " + nueva.nivel);
                    break;
                case "4":
                    System.out.print("Ingresa la persona que quieres mover del registro al equipo: ");
                    String agregar = escaner.nextLine();
                    if (!userView.usuario.hasOwned(agregar)) {
                        System.out.println("La Persona no está registrada y no se puede agregar.");
                    }
                    else if (userView.addToTeam(agregar, mU)) {
                        System.out.println("Persona añadida al equipo correctamente.");
                    }
                    else {
                        System.out.println("No se pudo agregar, revisa el nombre que ingresaste.");
                    }
                    break;
                case "5":
                    submenu = false;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }
    public void verSocialLinks() {
        try {
            usuario.managerUsuario mU = usuario.new managerUsuario();
            usuario.User tu = mU.getUsuarioActivo();

            if (tu == null) {
                System.out.println("No hay usuario activo. Por favor, inicia sesión o crea un usuario.");
                return;
            }

            Path personasDir = Paths.get("Data/personas");
            ObjectMapper mapper = new ObjectMapper();
            List<modelos.engine.Persona> personas = new ArrayList<>();

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(personasDir, "*.json")) {
                for (Path p : ds) {
                    modelos.engine.Persona persona = mapper.readValue(p.toFile(), modelos.engine.Persona.class);
                    personas.add(persona);
                }
            }

            modelos.engine.Registro registro = new modelos.engine.Registro();
            registro.buildFrom(personas);

            modelos.engine.indiceFusiones indiceF = new modelos.engine.indiceFusiones();
            modelos.engine.grafoSocialLinks socialGraph = new modelos.engine.grafoSocialLinks();
            modelos.engine.indicePorNivel levelIndex = new modelos.engine.indicePorNivel();
            indiceF.construirDe(personas);
            levelIndex.buildFrom(personas);

            ObjectMapper mapperSL = new ObjectMapper();
            Path socialLinksPath = Paths.get("Data/socialLinks.json");
            if (Files.exists(socialLinksPath)) {
                modelos.engine.SocialLinkData slData = mapperSL.readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
                socialGraph.construirDesdeSocialLinks(slData.socialLinks);
            } else {
                socialGraph.construirDesdePersonas(personas);
            }

            usuario.UserView userView = new usuario().new UserView(tu, registro, indiceF, socialGraph, levelIndex, 12);

            boolean submenu = true;
            final String BOLD = "\u001B[1m";
            final String RESET = "\u001B[0m";
            while (submenu) {
                consola.mostrarMenuSocialLinks();
                System.out.println("Mes actual: " + tu.mes);
                System.out.print("Opción: ");
                String opcion = escaner.nextLine();
                switch (opcion) {
                    case "1":
                        System.out.println(BOLD + "Social links del usuario:" + RESET);
                        List<String> lines = new ArrayList<>();
                        for (Map.Entry<String, Integer> entry : tu.socialLinks.entrySet()) {
                            String npc = entry.getKey();
                            int nivel = entry.getValue();
                            String arcano = socialGraph.getArcano(npc);
                            String unlocked = socialGraph.getUnlockedPersona(npc);
                            String bloquea = unlocked == null ? "ninguno" : unlocked;
                            lines.add(String.format(" %s%s%s [%s] (Lv %d) -> %s |", BOLD, npc, RESET, arcano, nivel, bloquea));
                        }
                        int half = (lines.size() + 1) / 2;
                        for (int i = 0; i < half; i++) {
                            String left = String.format("%-42s", lines.get(i));
                            String right = i + half < lines.size() ? lines.get(i + half) : "";
                            System.out.println(left + right);
                        }
                        System.out.print("\nIngresa el nombre del NPC para ver detalles (enter para regresar): ");
                        String seleccionado = escaner.nextLine();
                        if (!seleccionado.isBlank()) {
                            if (!tu.socialLinks.containsKey(seleccionado)) {
                                System.out.println("NPC no encontrado en tus social links.");
                            } else {
                                int nivel = tu.getSocialLinkLevel(seleccionado);
                                int requerido = socialGraph.getRequiredLevel(seleccionado);
                                String desbloqueo = socialGraph.getUnlockedPersona(seleccionado);
                                String arcano = socialGraph.getArcano(seleccionado);
                                String requisitos = socialGraph.getRequirementsDescription(seleccionado);
                                System.out.println(BOLD + "NPC:" + RESET + " " + BOLD + seleccionado + RESET);
                                System.out.println(BOLD + "Arcano:" + RESET + " " + arcano);
                                System.out.println(BOLD + "Nivel:" + RESET + " " + nivel + "/" + requerido);
                                System.out.println(BOLD + "Desbloquea:" + RESET + " " + (desbloqueo == null ? "ninguno" : desbloqueo));
                                System.out.println(BOLD + "Requisitos:" + RESET + " " + requisitos);
                                System.out.println(BOLD + "Estado:" + RESET + " " + (nivel >= requerido ? "desbloqueado" : "bloqueado"));
                            }
                        }
                        break;
                    case "2":
                        System.out.print("\nIngresa el NPC para aumentar su nivel: ");
                        String npcName = escaner.nextLine();
                        if (!tu.socialLinks.containsKey(npcName)) {
                            System.out.println("\nNPC no encontrado en tus social links.");
                            break;
                        }
                        String restriction = socialGraph.getIncreaseRestrictionReason(npcName, tu.mes, tu.socialLinks);
                        if (restriction != null) {
                            System.out.println("No puedes subir este Social Link ahora: " + restriction);
                            break;
                        }
                        String nuevoDesbloqueo = userView.increaseSocialLinkAndHandleUnlock(npcName, 1, mU);
                        System.out.println(BOLD + "Nivel de " + npcName + RESET + " ahora es " + tu.getSocialLinkLevel(npcName));
                        if (nuevoDesbloqueo != null) {
                            System.out.println(BOLD + "Has desbloqueado:" + RESET + " " + nuevoDesbloqueo);
                        }
                        break;
                    case "3":
                        tu.mes = tu.mes % 13 + 1;
                        mU.saveUser(tu);
                        System.out.println("Ahora estás en el mes " + tu.mes);
                        break;
                    case "4":
                        submenu = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al gestionar Social Links: " + e.getMessage());
        }
    }
    public void buscarPersona() {
        // Implementar la lógica para buscar una persona
    }
    public void verFusionesEspeciales() {
        // Implementar la lógica para ver y realizar las fusiones especiales
    }
    public void cambiarUsuario() {
        // Implementar la lógica para cambiar de usuario
        boolean bandera = false;
        boolean flag = true;
        while (flag == true) {
            System.out.print("¿Desea cambiar de usuario? (s/n): ");
            String opcion = escaner.nextLine();
            switch (opcion.toLowerCase()) {
                case "s" : {
                    bandera = true;
                    flag = false;
                    break;
                }
                case "n" : {
                // Se rompe el bucle de selección pero no se continua el proceso de cambio de usuario
                    flag = false;    
                    break;
                }
                default:
                     System.out.println("Opción no válida. Por favor, seleccione 's' o 'n'.");
                break;     
            }
            // Se rompe el bucle de selección y se continua el proceso de cambio de usuario
                    }
        if (bandera == true){
            try {
                usuario.seleccionarUsuario();
            } 
            catch (Exception e) {
                System.out.println("Error al escoger usuario: " + e.getMessage());
            }
        }
    }
}