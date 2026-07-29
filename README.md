# Asistente de Fusiones Persona 3 Reload (APR)
## Proyecto para la asignatura Estructura de Datos - Grupo 4
![Logo](https://images7.alphacoders.com/138/thumb-1920-1384218.jpg)
#### Integrantes
  - Mauricio Cepeda
  - Lucas García
  - Camilo Ortega
  - Cristian David Prada

## Planteamiento

APR es un proyecto pensando para los jugadores de la saga persona los que muchas veces, ante tanto contenido diverso y entretenido, se ven un tanto desmotivados por lo lento, abstracto y tedioso que representa el sistema de **"Fusión de Personas"**, ya que dentro del juego no hay ninguna guía o apoyo para que el jugador pueda ser eficiente a la hora de realizar estas fusiones y obtener aquellas personas que necesita en específico para algún combate y/o misión.


El presente proyecto busca aplicar los conocimientos obtenidos en la asignatura ya mencionada, para implementar dichas estructuras de datos de forma óptima, evidenciando el aprendizaje que los miembros del grupo tuvieron a lo largo del semestre, de una manera didáctica con un tema tan casual como un videojuego.

## Objetivos
### General
- Facilitar el proceso de fusión de personas, al darle al usuario una experiencia personalizada manualmente, con la que pueda obtener toda la información necesaria para este apartado del juego, y que incluso pueda servir como simulador al promover que el usuario intente y vea las distintas opciones sin comprometer su partida.
### Específicos
- Implementar las estructuras de datos para resolver el planteamiento ya mencionado.
- Proporcionar el registro completo y abierto de todas las personas a las que puede acceder el jugador, junto con las condiciones para ello.
- Permitir al usuario el interactuar con un equipo virtual que le muestre las fusiones posibles entre sus miembros.
- Entregar la opción de Búsqueda de una o varias Personas en específico, a través de filtros que reduzcan la amplitud del grupo.
- Asociar información de registro y equipo actual para cada usuario del programa, asegurándolas en clave.
- Representar gráficamente tanto las fusiones, como las personas, equipo y registro.

## Programa

### Estructura del proyecto

```text
APR/
│
├──  .vscode/             # Settings
│   └── settings.json    
│
├──  Data/         # Guarda toda la información
│   ├── personas/     # Info de cada Persona
│   ├── pictures/ # Foto de los NPC y Personas
│   ├── users/   # Usuarios Registrados 
│   └── socialLinks.json     # Info Social Links
│
├──  Interfaz/             # Estructura básica con la que el usuario interactua
│   └── interfaz.java
│
├──  lab/         # Dependencias necesarias
│
├──  modelos/             # Esqueleto del programa y clases básicas
│   ├── engine.java   # Carga al inicio toda la información de Data/ y la organiza en las clases
│   └── usuarios.java  # Administra todo el apartado de usuarios y su información propia
│
├──  vista/             # Guarda los String a imprimir en consola en caso de ejecutar el programa localmente
│   └── consola.java
│
└──  Main/             # Punto de Entrada
    └── Main.java 
```
### Clases Principales

## Implementación

### Persona
![Logo2](https://samurai-gamers.com/wp-content/uploads/2024/08/sg-p3r-chariot-thor-1024x576.jpg)

Se instancian los objetos "Persona" con sus respectivos atributos (nombre, arcano, etc.), y asociandolo con la estructura implementada en la base de datos, Json, permitiendo la construcción de todos los objetos al inicio del programa.
```java
public static class Persona {
        public String nombre;
        public String arcano;
        public int nivel;
        public List<FusionEntry> posiblesFusiones;
        public List<GeneratedByEntry> generadoPor;
        public List<specialEntry> fusionEspecial;
        public requiresEntry requisitoFusion;
        public Map<String,Object> estadisticas;

        public Persona() {}
        @Override public String toString() { return nombre + " [" + arcano + " Lv:" + nivel + "]"; }
    }

```
### Equipo
[![image.png](https://preview.redd.it/i-have-a-question-about-the-persona-compendium-v0-l9by94f2acxe1.jpeg?width=3840&format=pjpg&auto=webp&s=528a38bb289451b88a728c377c6b680152513394)

Para cada usuario, se le asigna un arreglo simple con una capacidad máxima de Personas y que puede ser editado en cualquier momento.
```java
public static class Equipo {
        public final Persona[] miembros;
        private final int capacidad;

        public Equipo(int capacidad) {
            this.capacidad = capacidad;
            this.miembros = new Persona[capacidad];
        }
```
### Registro
![Logo3](https://imag.malavida.com/mvimgbig/download-fs/persona-3-reload-38887-1.jpg)

A través de una lista enlazada y de las Personas ya instanciadas, al inicio del programa se construye el registro a partir de estos datos, manteniendo un orden estricto.
```java
    public static class Registro {
        public static class Node {
            Persona dato;
            Node next;
            Node(Persona p) { dato = p; next = null; }
        }
        private Node cabeza;
        private int tamaño;
        private final Map<String, Node> indicePorNombre = new HashMap<>(); // índice auxiliar name -> node

        public Registro() { cabeza = null; tamaño = 0; }
```

### Social Link
![Logo4](https://preview.redd.it/1-year-of-social-links-in-p3r-visualized-v0-0lb5q6zz54pc1.png?width=1400&format=png&auto=webp&s=5ed04a92a4d2a96b094b8dab8495e8c09e154ba2)

Con la ayuda de un grafo, cada NPC (non-playable character) se logra instanciar al inicio del programa a partir de la base de datos, contando con sus atributos específicos, y las dependencias y relaciones con otros.
```java
public static class NPC {
        public String nombre;
        public String arcano;
        public int nivelActual;
        public int nivelMaximo;
        public String desbloquea;
        public List<Requisito> requisitos = new ArrayList<>();

        public NPC() {}
        public NPC(String nombre, int nivelMaximo) {
            this.nombre = nombre;
            this.nivelMaximo = nivelMaximo;
            this.nivelActual = 0;
            this.requisitos = new ArrayList<>();
        }
```
## ¿Cómo usar el programa?
![Logo](https://images4.alphacoders.com/137/thumb-1920-1370601.jpeg)
Para poder utilizar el programa hay 2 opciones, idealmente puedes usar la versión web, en donde solo debes ingresar al siguiente [link](https://aistudio.google.com/apps/1d0f9adb-cabc-4984-b762-30f0034d6d13?showAssistant=true&showPreview=true) y encontrarás una interfaz gráfica y ayudas visuales que no solo facilitan, sino que amenizan el uso del programa.

No obstante, si quieres tener el programa localmente en tu computadora, debes tener en cuenta la siguiente información:
### Requerimientos
Para que el programa funcione deben seguir los siguientes pasos:
- **Primero**: Hay que instalar el lenguaje de programación "java" en el sistema operativo.
Para esto se debe ingresar a la página oficial y [descargar java](https://www.java.com/es/download/manual.jsp) en la versión más actual posible para tu sistema operativo.

Si estas usando Windows puedes comprobar que la instalación haya funcionado abriendo la consola de Windows, presionando (win + r), y escribir "java --version", si funcionó debería responder con la versión descargada y ya se tendría al interprete instalado.

- **Segundo**: Debes descargar los archivos y carpetas dentro de este repositorio y abrirlas en un editor de código como puede ser [Visual Studio Code](https://code.visualstudio.com/download).

- Y **Tecero**: lógicamente descargar los archivos del repositorio, los cuales deberás abrir en una carpeta dentro del editor de código que hayas preferido. 

### Cómo usarlo
Para usar el programa es bastante sencillo, el usuario debe ingresar al archivo main.java y ejecutar el programa con el boton integrado en el editor de código.
