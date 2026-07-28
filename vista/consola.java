package vista;


/**
 * Clase que imprime en consola la interfaz con la que interactua el usuario.
 */
public class consola {

    public consola() {
    }

    /**
     * Muestra el mensaje de bienvenida.
     */
    public void mostrarBienvenida() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║           Bienvenido al Asistente de Fusiones        ║");
        System.out.println("║    Enfoncado en Persona 3 R  - Estructuras de Datos  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    public void mostrarMenuPrincipal() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║               Selecciona una opción válida           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                  1. Ver tu Equipo actual             ║");
        System.out.println("║                  2. Ver tu Registro                  ║");
        System.out.println("║                  3. Ver tus Social Links             ║");
        System.out.println("║                  4. Buscar una Persona               ║");
        System.out.println("║                  5. Ver Fusiones Especiales          ║");
        System.out.println("║                  6. Cambiar de Usuario               ║");
        System.out.println("║                  7. Salir                            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    public synchronized void menuUsuarios() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║               Selecciona una opción válida           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                  1. Iniciar Sesión                   ║");
        System.out.println("║                  2. Crear Usuario                    ║");
        System.out.println("║                  3. Regresar                         ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    public synchronized void mostrarMenuEquipo() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                   MENÚ DEL EQUIPO                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║          1. Ver fusiones disponibles                ║");
        System.out.println("║          2. Agregar persona al equipo               ║");
        System.out.println("║          3. Liberar persona del equipo              ║");
        System.out.println("║          4. Volver al menú principal                ║");
        System.out.println("╚═════════════════════════════════════════════════════╝");
    }

    public synchronized void mostrarMenuRegistro() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                 MENÚ DEL REGISTRO                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║          1. Ver registro por nivel                  ║");
        System.out.println("║          2. Ver registro por arcano                 ║");
        System.out.println("║          3. Registrar persona manualmente           ║");
        System.out.println("║          4. Agregar persona del registro al equipo  ║");
        System.out.println("║          5. Volver al menú principal                ║");
        System.out.println("╚═════════════════════════════════════════════════════╝");
    }

    public synchronized void mostrarMenuSocialLinks() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                 MENÚ DE SOCIAL LINKS                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║          1. Mostrar social links                    ║");
        System.out.println("║          2. Incrementar social link                 ║");
        System.out.println("║          3. Avanzar el mes                          ║");
        System.out.println("║          4. Volver al menú principal                ║");
        System.out.println("╚═════════════════════════════════════════════════════╝");
    }

    public synchronized void mostrarMenuBusqueda() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║                 MENÚ DE BÚSQUEDA                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          1. Buscar por nombre exacto                 ║");
        System.out.println("║          2. Buscar por arcano                        ║");
        System.out.println("║          3. Buscar nivel mayor a                     ║");
        System.out.println("║          4. Buscar nivel menor a                     ║");
        System.out.println("║          5. Buscar nivel en rango                    ║");
        System.out.println("║          6. Volver al menú principal                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    public synchronized void mostrarMenuFusionesEspeciales() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║             MENÚ DE FUSIONES ESPECIALES              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}