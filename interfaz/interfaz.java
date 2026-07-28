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
        tu.mes = 4;
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

    private List<modelos.engine.Persona> cargarPersonas() throws Exception {
        Path personasDir = Paths.get("Data/personas");
        ObjectMapper mapper = new ObjectMapper();
        List<modelos.engine.Persona> personas = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(personasDir, "*.json")) {
            for (Path p : ds) {
                modelos.engine.Persona persona = mapper.readValue(p.toFile(), modelos.engine.Persona.class);
                personas.add(persona);
            }
        }

        return personas;
    }

    private List<String> parseIngredientNames(List<String> source) {
        List<String> ingredients = new ArrayList<>();
        if (source == null) return ingredients;
        for (String item : source) {
            if (item == null) continue;
            for (String part : item.split(",")) {
                String name = part.trim();
                if (!name.isEmpty()) {
                    ingredients.add(name);
                }
            }
        }
        return ingredients;
    }

    private boolean isTeamMember(String name, usuario.UserView userView) {
        if (name == null || name.isBlank() || userView == null) return false;
        for (modelos.engine.Persona miembro : userView.equipo.getMiembros()) {
            if (miembro != null && miembro.nombre != null && miembro.nombre.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private String formatIngredient(String ingredient, usuario.UserView userView) {
        if (ingredient == null || ingredient.isBlank()) return "";
        String formatted = ingredient.trim();
        if (isTeamMember(formatted, userView)) {
            formatted += " (EN EQUIPO)";
        }
        return formatted;
    }

    private void mostrarDetallesPersona(modelos.engine.Persona persona, usuario.UserView userView) {
        usuario.User tu = userView.usuario;
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          Detalles de la Persona         ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Nombre: " + persona.nombre);
        System.out.println("Arcano: " + persona.arcano);
        System.out.println("Nivel: " + persona.nivel);
        System.out.println("Registrada: " + (tu.hasOwned(persona.nombre) ? "Sí" : "No"));
        System.out.println("En equipo: " + (isTeamMember(persona.nombre, userView) ? "Sí" : "No"));

        if (persona.estadisticas != null && !persona.estadisticas.isEmpty()) {
            System.out.println("Estadísticas:");
            persona.estadisticas.forEach((k, v) -> System.out.println("  - " + k + ": " + v));
        }

        if (persona.generadoPor != null && !persona.generadoPor.isEmpty()) {
            System.out.println("\nFusiones normales para obtener a " + persona.nombre + ":");
            int contador = 1;
            for (modelos.engine.GeneratedByEntry entry : persona.generadoPor) {
                List<String> ingredients = parseIngredientNames(entry.de);
                if (ingredients.isEmpty()) continue;
                List<String> decorated = new ArrayList<>();
                int teamCount = 0;
                for (String ingredient : ingredients) {
                    if (isTeamMember(ingredient, userView)) {
                        decorated.add(ingredient + " (EN EQUIPO)");
                        teamCount++;
                    } else if (tu.hasOwned(ingredient)) {
                        decorated.add(ingredient + " (REGISTRADA)");
                    } else {
                        decorated.add(ingredient);
                    }
                }
                String prefix = teamCount > 0 ? "* " : "  ";
                System.out.println(prefix + contador++ + ". " + String.join(", ", decorated));
            }
        } else {
            System.out.println("\nNo se encontraron fusiones normales registradas para esta persona.");
        }

        if (persona.fusionEspecial != null && !persona.fusionEspecial.isEmpty()) {
            System.out.println("\nFusión especial disponible para esta persona:");
            int contador = 1;
            for (modelos.engine.specialEntry entry : persona.fusionEspecial) {
                List<String> ingredients = parseIngredientNames(entry.de);
                if (ingredients.isEmpty()) continue;
                List<String> decorated = new ArrayList<>();
                for (String ingredient : ingredients) {
                    if (isTeamMember(ingredient, userView)) {
                        decorated.add(ingredient + " (EN EQUIPO)");
                    } else if (tu.hasOwned(ingredient)) {
                        decorated.add(ingredient + " (REGISTRADA)");
                    } else {
                        decorated.add(ingredient);
                    }
                }
                System.out.println("  " + contador++ + ". " + String.join(", ", decorated));
            }
        } else {
            System.out.println("\nNo hay fusiones especiales para esta persona.");
        }
    }

    public void buscarPersona() {
        try {
            usuario.managerUsuario mU = usuario.new managerUsuario();
            usuario.User tu = mU.getUsuarioActivo();
            if (tu == null) {
                System.out.println("No hay usuario activo. Por favor, inicia sesión o crea un usuario.");
                return;
            }

            List<modelos.engine.Persona> personas = cargarPersonas();
            modelos.engine.Registro registro = new modelos.engine.Registro();
            registro.buildFrom(personas);
            modelos.engine.indiceFusiones indiceF = new modelos.engine.indiceFusiones();
            modelos.engine.grafoSocialLinks socialGraph = new modelos.engine.grafoSocialLinks();
            modelos.engine.indicePorNivel levelIndex = new modelos.engine.indicePorNivel();
            indiceF.construirDe(personas);
            levelIndex.buildFrom(personas);
            Path socialLinksPath = Paths.get("Data/socialLinks.json");
            if (Files.exists(socialLinksPath)) {
                modelos.engine.SocialLinkData slData = new ObjectMapper().readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
                socialGraph.construirDesdeSocialLinks(slData.socialLinks);
            } else {
                socialGraph.construirDesdePersonas(personas);
            }

            usuario.UserView userView = new usuario().new UserView(tu, registro, indiceF, socialGraph, levelIndex, 12);

            boolean submenu = true;
            while (submenu) {
                consola.mostrarMenuBusqueda();
                System.out.print("Opción: ");
                String opcion = escaner.nextLine();
                List<modelos.engine.Persona> resultados = new ArrayList<>();

                switch (opcion) {
                    case "1":
                        System.out.print("Nombre exacto: ");
                        String nombre = escaner.nextLine();
                        if (!nombre.isBlank()) {
                            modelos.engine.Persona p = registro.buscarPorNombre(nombre.trim());
                            if (p != null) resultados.add(p);
                        }
                        break;
                    case "2":
                        System.out.print("Arcano: ");
                        String arcano = escaner.nextLine().trim();
                        for (modelos.engine.Persona p : registro.toList()) {
                            if (p.arcano != null && p.arcano.equalsIgnoreCase(arcano)) resultados.add(p);
                        }
                        break;
                    case "3":
                        System.out.print("Nivel mayor a: ");
                        try {
                            int nivel = Integer.parseInt(escaner.nextLine().trim());
                            for (modelos.engine.Persona p : registro.toList()) {
                                if (p.nivel > nivel) resultados.add(p);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Nivel inválido.");
                        }
                        break;
                    case "4":
                        System.out.print("Nivel menor a: ");
                        try {
                            int nivel = Integer.parseInt(escaner.nextLine().trim());
                            for (modelos.engine.Persona p : registro.toList()) {
                                if (p.nivel < nivel) resultados.add(p);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Nivel inválido.");
                        }
                        break;
                    case "5":
                        System.out.print("Nivel mínimo: ");
                        String minText = escaner.nextLine().trim();
                        System.out.print("Nivel máximo: ");
                        String maxText = escaner.nextLine().trim();
                        try {
                            int min = Integer.parseInt(minText);
                            int max = Integer.parseInt(maxText);
                            for (modelos.engine.Persona p : registro.toList()) {
                                if (p.nivel >= min && p.nivel <= max) resultados.add(p);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Nivel inválido.");
                        }
                        break;
                    case "6":
                        submenu = false;
                        continue;
                    default:
                        System.out.println("Opción no válida.");
                        continue;
                }

                if (resultados.isEmpty()) {
                    System.out.println("No se encontraron personas con los filtros seleccionados.");
                    continue;
                }

                System.out.println("\nResultados:");
                for (int i = 0; i < resultados.size(); i++) {
                    modelos.engine.Persona p = resultados.get(i);
                    String etiqueta = isTeamMember(p.nombre, userView) ? "[EQUIPO]" : (tu.hasOwned(p.nombre) ? "[REG]" : "");
                    System.out.println((i + 1) + ". " + p.nombre + " " + etiqueta + " - " + p.arcano + " Lv " + p.nivel);
                }

                System.out.print("Selecciona el número para ver detalles (enter para regresar): ");
                String seleccion = escaner.nextLine().trim();
                if (seleccion.isBlank()) continue;
                try {
                    int indice = Integer.parseInt(seleccion) - 1;
                    if (indice < 0 || indice >= resultados.size()) {
                        System.out.println("Selección inválida.");
                        continue;
                    }
                    mostrarDetallesPersona(resultados.get(indice), userView);
                } catch (NumberFormatException ex) {
                    System.out.println("Selección inválida.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al buscar persona: " + e.getMessage());
        }
    }

    private List<List<String>> obtenerRecetasEspeciales(modelos.engine.Persona persona) {
        List<List<String>> recetas = new ArrayList<>();
        if (persona == null || persona.fusionEspecial == null) return recetas;
        for (modelos.engine.specialEntry entry : persona.fusionEspecial) {
            List<String> ingredients = parseIngredientNames(entry.de);
            if (!ingredients.isEmpty()) recetas.add(ingredients);
        }
        return recetas;
    }

    private String buildSpecialFusionDescription(modelos.engine.Persona persona, usuario.UserView userView) {
        List<List<String>> recetas = obtenerRecetasEspeciales(persona);
        if (recetas.isEmpty()) return "Sin datos de fusión especial";
        List<String> lines = new ArrayList<>();
        for (List<String> receta : recetas) {
            List<String> decorated = new ArrayList<>();
            for (String ingredient : receta) {
                if (isTeamMember(ingredient, userView)) {
                    decorated.add(ingredient + " (EN EQUIPO)");
                } else if (userView.usuario.hasOwned(ingredient)) {
                    decorated.add(ingredient + " (REGISTRADA)");
                } else {
                    decorated.add(ingredient);
                }
            }
            lines.add(String.join(", ", decorated));
        }
        return String.join(" | ", lines);
    }

    private void realizarFusionEspecial(modelos.engine.Persona objetivo, usuario.UserView userView, usuario.managerUsuario mU) throws Exception {
        if (objetivo == null) return;
        if (userView.isOnTeam(objetivo.nombre, mU)) {
            System.out.println("Este persona ya está en tu equipo.");
            return;
        }
        List<List<String>> recetas = obtenerRecetasEspeciales(objetivo);
        if (recetas.isEmpty()) {
            System.out.println("No existe una receta especial válida para esta persona.");
            return;
        }
        List<String> ingredientes = recetas.get(0);

        List<String> faltantes = new ArrayList<>();
        List<String> aLiberar = new ArrayList<>();
        for (String ing : ingredientes) {
            if (isTeamMember(ing, userView)) {
                aLiberar.add(ing);
            } else if (!userView.usuario.hasOwned(ing)) {
                faltantes.add(ing);
            }
        }

        if (!faltantes.isEmpty()) {
            System.out.println("No puedes realizar la fusión porque te faltan las siguientes personas:");
            for (String faltante : faltantes) {
                System.out.println(" - " + faltante);
            }
            return;
        }

        for (String nombre : aLiberar) {
            userView.equipo.liberarPersona(nombre);
            userView.usuario.removeTeamMember(nombre);
        }

        if (!userView.usuario.hasOwned(objetivo.nombre)) {
            userView.usuario.registerOwned(objetivo.nombre);
        }

        if (!userView.equipo.tieneEspacio()) {
            System.out.println("No hay espacio disponible en el equipo después de liberar los ingredientes.");
            return;
        }

        boolean added = userView.equipo.agregarPersona(objetivo);
        if (added) {
            userView.usuario.addTeamMember(objetivo.nombre);
        }
        mU.saveUser(userView.usuario);
        System.out.println("Fusión especial realizada con éxito. " + objetivo.nombre + " ha sido agregado al equipo.");
    }

    public void verFusionesEspeciales() {
        try {
            usuario.managerUsuario mU = usuario.new managerUsuario();
            usuario.User tu = mU.getUsuarioActivo();
            if (tu == null) {
                System.out.println("No hay usuario activo. Por favor, inicia sesión o crea un usuario.");
                return;
            }

            List<modelos.engine.Persona> personas = cargarPersonas();
            modelos.engine.Registro registro = new modelos.engine.Registro();
            registro.buildFrom(personas);
            modelos.engine.indiceFusiones indiceF = new modelos.engine.indiceFusiones();
            modelos.engine.grafoSocialLinks socialGraph = new modelos.engine.grafoSocialLinks();
            modelos.engine.indicePorNivel levelIndex = new modelos.engine.indicePorNivel();
            indiceF.construirDe(personas);
            levelIndex.buildFrom(personas);
            Path socialLinksPath = Paths.get("Data/socialLinks.json");
            if (Files.exists(socialLinksPath)) {
                modelos.engine.SocialLinkData slData = new ObjectMapper().readValue(socialLinksPath.toFile(), modelos.engine.SocialLinkData.class);
                socialGraph.construirDesdeSocialLinks(slData.socialLinks);
            } else {
                socialGraph.construirDesdePersonas(personas);
            }

            usuario.UserView userView = new usuario().new UserView(tu, registro, indiceF, socialGraph, levelIndex, 12);
            List<modelos.engine.Persona> especiales = new ArrayList<>();
            for (modelos.engine.Persona p : registro.toList()) {
                if (p.fusionEspecial != null && !p.fusionEspecial.isEmpty()) {
                    especiales.add(p);
                }
            }

            if (especiales.isEmpty()) {
                System.out.println("No hay fusiones especiales disponibles en el registro.");
                return;
            }

            boolean submenu = true;
            while (submenu) {
                consola.mostrarMenuFusionesEspeciales();
                for (int i = 0; i < especiales.size(); i++) {
                    modelos.engine.Persona p = especiales.get(i);
                    String descripcion = buildSpecialFusionDescription(p, userView);
                    System.out.println((i + 1) + ". " + p.nombre + " [" + p.arcano + " Lv " + p.nivel + "] -> " + descripcion);
                }
                System.out.println("0. Volver");
                System.out.print("Selecciona la fusión especial a realizar: ");
                String opcion = escaner.nextLine().trim();
                if (opcion.equals("0")) {
                    submenu = false;
                    continue;
                }
                int seleccion;
                try {
                    seleccion = Integer.parseInt(opcion);
                } catch (NumberFormatException ex) {
                    System.out.println("Opción inválida.");
                    continue;
                }

                if (seleccion < 1 || seleccion > especiales.size()) {
                    System.out.println("Selección inválida.");
                    continue;
                }

                modelos.engine.Persona elegido = especiales.get(seleccion - 1);
                System.out.println("\nHas seleccionado la fusión especial de " + elegido.nombre + ".");
                System.out.println("Receta: " + buildSpecialFusionDescription(elegido, userView));
                System.out.print("¿Deseas realizarla? (s/n): ");
                String confirmar = escaner.nextLine().trim().toLowerCase();
                if (confirmar.equals("s")) {
                    realizarFusionEspecial(elegido, userView, mU);
                } else {
                    System.out.println("Fusión especial cancelada.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al mostrar fusiones especiales: " + e.getMessage());
        }
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