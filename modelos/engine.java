package modelos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class engine {

    /* ---------------------------
    MODELOS JSON / DOM
    --------------------------- */
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

    public static class FusionEntry {
        public List<String> con;   // Pareja de fusión
        public String resultado; // Persona obtenida
    }

    public static class GeneratedByEntry {
        public List<String> de; // Lista de personas
    }

    public static class specialEntry {
        public List<String> de; // Lista de personas
    }

    public static class requiresEntry {
        public String socialLink;
    }

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
    }

    public static class Requisito {
        public String npcDependiente; // nombre del otro NPC
        public int nivelNecesario;    // rango mínimo requerido
        public int mesMinimo;         // mes a partir del cual se puede subir
    }

    public static class SocialLinkData {
        public List<SocialLinkEntry> socialLinks;
    }

    public static class SocialLinkEntry {
        public String npc;
        public String arcano;
        public int nivelMaximo;
        public List<String> desbloquea;
        public List<Requisito> requisitos;
    }

    /* ---------------------------
    REGISTRY: Lista enlazada + índice auxiliar
    --------------------------- */
    public static class Registro {
        public static class Node {
            Persona dato;
            Node next;
            Node(Persona p) { dato = p; next = null; }
        }
        private Node cabeza;
        private int tamaño;
        private final Hash<String, Node> indicePorNombre = new Hash<>(); // índice auxiliar name -> node

        public Registro() { cabeza = null; tamaño = 0; }

        // Construye la lista enlazada desde colección (solo al inicio)
        public void buildFrom(Collection<Persona> personas) {
            Node cola = null;
            for (Persona p : personas) {
                Node n = new Node(p);
                if (cabeza == null) { cabeza = n; cola = n; }
                else { cola.next = n; cola = n; }
                indicePorNombre.put(p.nombre.toLowerCase(), n);
                tamaño++;
            }
        }

        public Persona buscarPorNombre(String name) {
            Node n = indicePorNombre.get(name.toLowerCase());
            return n == null ? null : n.dato;
        }

        public Node nodoPorNombre(String name) { return indicePorNombre.get(name.toLowerCase()); }

        public List<Persona> toList() {
            List<Persona> out = new ArrayList<>();
            Node cur = cabeza;
            while (cur != null) { out.add(cur.dato); cur = cur.next; }
            return out;
        }

        public int size() { return tamaño; }

        // Imprime el registro completo (orden de carga)
        public void mostrarRegistro() {
            Node cur = cabeza;
            while (cur != null) {
                System.out.println(cur.dato);
                cur = cur.next;
            }
        }
    }

    /* ---------------------------
    indicePorNivel: Permite consultar personas desde ciertos rangos
    --------------------------- */
    public static class indicePorNivel {
        private final Arbol<Integer, List<Persona>> indice = new Arbol<>();

        public void buildFrom(Collection<Persona> personas) {
            indice.clear();
            for (Persona p : personas) {
                indice.computeIfAbsent(p.nivel, k -> new ArrayList<>()).add(p);
            }
        }

        // Consulta por rango inclusive
        public List<Persona> busquedaPorRango(int min, int max) {
            List<Persona> out = new ArrayList<>();
            NavigableMap<Integer, List<Persona>> sub = indice.subMap(min, true, max, true);
            for (List<Persona> list : sub.values()) out.addAll(list);
            return out;
        }

        // Obtener top N por nivel descendente
        public List<Persona> topN(int n) {
            List<Persona> out = new ArrayList<>();
            for (Integer lvl : indice.descendingKeySet()) {
                for (Persona p : indice.get(lvl)) {
                    out.add(p);
                    if (out.size() >= n) return out;
                }
            }
            return out;
        }
    }

    /* ---------------------------
    SocialGraph: NPCs y desbloqueos
    --------------------------- */
    public static class grafoSocialLinks {
        private final Hash<String, NPC> npcs = new Hash<>();
        private final Hash<String, String> aliasToCanonical = new Hash<>();

        private String normalizeKey(String nombre) {
            if (nombre == null) return null;
            return nombre.trim().toLowerCase();
        }

        private String resolveName(String nombre) {
            String normalized = normalizeKey(nombre);
            if (normalized == null) return null;
            String canonical = aliasToCanonical.get(normalized);
            if (canonical != null) return canonical;
            for (String key : npcs.keySet()) {
                if (normalizeKey(key).equals(normalized)) return key;
            }
            return nombre;
        }

        private int getSocialLinkLevel(String nombreNPC, Hash<String,Integer> socialLevels) {
            if (nombreNPC == null || socialLevels == null) return 0;
            Integer exact = socialLevels.get(nombreNPC);
            if (exact != null) return exact;
            String canonical = resolveName(nombreNPC);
            if (canonical != null && !canonical.equals(nombreNPC)) {
                Integer canon = socialLevels.get(canonical);
                if (canon != null) return canon;
            }
            for (java.util.Map.Entry<String,Integer> entry : socialLevels.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(nombreNPC)) return entry.getValue();
            }
            return 0;
        }

        public void construirDe(List<NPC> npcList) {
            npcs.clear();
            for (NPC n : npcList) {
                if (n == null || n.nombre == null) continue;
                npcs.put(n.nombre, n);
            }
        }

        public void construirDesdePersonas(Collection<Persona> personas) {
            npcs.clear();
            for (Persona p : personas) {
                if (p == null || p.requisitoFusion == null || p.requisitoFusion.socialLink == null || p.requisitoFusion.socialLink.isBlank()) continue;
                String npcName = p.requisitoFusion.socialLink;
                NPC npc = npcs.computeIfAbsent(npcName, k -> new NPC());
                npc.nombre = npcName;
                if (npc.desbloquea == null && p.nombre != null) {
                    npc.desbloquea = p.nombre;
                }
            }
        }

        public void construirDesdeSocialLinks(List<SocialLinkEntry> entries) {
            npcs.clear();
            aliasToCanonical.clear();
            if (entries == null) return;
            for (SocialLinkEntry entry : entries) {
                if (entry == null || entry.npc == null || entry.npc.isBlank()) continue;
                NPC npc = new NPC();
                npc.nombre = entry.npc;
                npc.arcano = entry.arcano;
                npc.nivelMaximo = entry.nivelMaximo;
                npc.nivelActual = 0;
                npc.desbloquea = (entry.desbloquea == null || entry.desbloquea.isEmpty()) ? null : entry.desbloquea.get(0);
                npc.requisitos = entry.requisitos == null ? new ArrayList<>() : entry.requisitos;
                npcs.put(npc.nombre, npc);
                if (npc.nombre.equalsIgnoreCase("Bunkichi y Mitsuko")) {
                    aliasToCanonical.put("bunkichi y mitsuku", npc.nombre);
                }
            }
        }

        public String getRequirementsDescription(String nombreNPC) {
            NPC npc = getNPC(nombreNPC);
            if (npc == null || npc.requisitos == null || npc.requisitos.isEmpty()) {
                return "Ninguno";
            }
            List<String> partes = new ArrayList<>();
            for (Requisito req : npc.requisitos) {
                StringBuilder sb = new StringBuilder();
                if (req.npcDependiente != null && !req.npcDependiente.isBlank()) {
                    sb.append("requiere ").append(req.npcDependiente).append(" lvl ").append(req.nivelNecesario);
                }
                if (req.mesMinimo > 0) {
                    if (sb.length() > 0) sb.append(" y ");
                    sb.append("mes >= ").append(req.mesMinimo);
                }
                if (sb.length() == 0) sb.append("ninguno");
                partes.add(sb.toString());
            }
            return String.join(", ", partes);
        }

        public void ensureNPC(String name) {
            if (name == null || name.isBlank()) return;
            String canonical = resolveName(name);
            if (!npcs.containsKey(canonical)) {
                npcs.put(canonical, new NPC(canonical, 10));
            }
        }

        public NPC getNPC(String name) {
            if (name == null) return null;
            String canonical = resolveName(name);
            if (canonical == null) return null;
            return npcs.get(canonical);
        }

        public String getUnlockedPersona(String nombreNPC) {
            NPC npc = getNPC(nombreNPC);
            if (npc == null || npc.desbloquea == null) return null;
            return npc.desbloquea;
        }

        public String getArcano(String nombreNPC) {
            NPC npc = getNPC(nombreNPC);
            return npc == null || npc.arcano == null ? "Desconocido" : npc.arcano;
        }

        public int getRequiredLevel(String nombreNPC) {
            NPC npc = getNPC(nombreNPC);
            return npc == null ? 10 : npc.nivelMaximo;
        }

        public boolean isFusionUnlocked(Persona resultado, usuario.User usuario) {
            if (resultado == null || resultado.requisitoFusion == null || resultado.requisitoFusion.socialLink == null || resultado.requisitoFusion.socialLink.isBlank()) {
                return true;
            }
            String npcName = resultado.requisitoFusion.socialLink;
            int level = usuario.getSocialLinkLevel(npcName);
            int required = getRequiredLevel(npcName);
            return level >= required;
        }

        public void ensureNPCsFor(Collection<String> nombres) {
            if (nombres == null) return;
            for (String nombre : nombres) {
                ensureNPC(nombre);
            }
        }

        public void syncLevels(Map<String,Integer> socialLevels) {
            if (socialLevels == null) return;
            for (java.util.Map.Entry<String,Integer> entry : socialLevels.entrySet()) {
                String npcName = entry.getKey();
                int level = entry.getValue() == null ? 0 : entry.getValue();
                String canonical = resolveName(npcName);
                ensureNPC(canonical);
                NPC npc = npcs.get(canonical);
                if (npc != null) {
                    npc.nivelActual = Math.max(0, Math.min(npc.nivelMaximo, level));
                }
            }
        }

        // Incrementa nivel; si llega al máximo devuelve la persona desbloqueada
        public String aumentarRango(String nombreNPC, int delta) {
            NPC npc = npcs.get(nombreNPC);
            if (npc == null) return null;
            int anterior = npc.nivelActual;
            npc.nivelActual = Math.min(npc.nivelMaximo, npc.nivelActual + delta);
            if (anterior < npc.nivelMaximo && npc.nivelActual >= npc.nivelMaximo) {
                return npc.desbloquea;
            }
            return null;
        }

        public boolean puedeSubir(String nombreNPC, int mesActual, Hash<String,Integer> socialLevels) {
            NPC npc = getNPC(nombreNPC);
            if (npc == null) return false;
            int nivelActual = getSocialLinkLevel(nombreNPC, socialLevels);
            if (nivelActual >= npc.nivelMaximo) return false;

            for (Requisito req : npc.requisitos) {
                if (req.npcDependiente != null && !req.npcDependiente.isBlank()) {
                    int nivelOtro = getSocialLinkLevel(req.npcDependiente, socialLevels);
                    if (nivelOtro < req.nivelNecesario) return false;
                }
                if (mesActual < req.mesMinimo) return false;
            }
            return true;
        }

        public String aumentarRango(String nombreNPC, int delta, int mesActual, Hash<String,Integer> socialLevels) {
            NPC npc = getNPC(nombreNPC);
            if (npc == null) return null;

            if (!puedeSubir(nombreNPC, mesActual, socialLevels)) {
                return null; // no cumple requisitos
            }

            int anterior = npc.nivelActual;
            npc.nivelActual = Math.min(npc.nivelMaximo, npc.nivelActual + delta);
            if (anterior < npc.nivelMaximo && npc.nivelActual >= npc.nivelMaximo) {
                return npc.desbloquea;
            }
            return null;
        }

        public boolean canIncreaseRank(String nombreNPC, int mesActual, Hash<String,Integer> socialLevels) {
            return puedeSubir(nombreNPC, mesActual, socialLevels);
        }

        public String getIncreaseRestrictionReason(String nombreNPC, int mesActual, Hash<String,Integer> socialLevels) {
            NPC npc = getNPC(nombreNPC);
            if (npc == null) return "Social Link no encontrado.";
            int nivelActual = getSocialLinkLevel(nombreNPC, socialLevels);
            if (nivelActual >= npc.nivelMaximo) {
                return "Ya alcanzaste el nivel máximo de este Social Link.";
            }
            for (Requisito req : npc.requisitos) {
                if (req.mesMinimo > 0 && mesActual < req.mesMinimo) {
                    return "Sólo puedes subir este Social Link a partir del mes " + req.mesMinimo + ".";
                }
                if (req.npcDependiente != null && !req.npcDependiente.isBlank()) {
                    int nivelOtro = getSocialLinkLevel(req.npcDependiente, socialLevels);
                    if (nivelOtro < req.nivelNecesario) {
                        return "Requiere Social Link '" + req.npcDependiente + "' nivel " + req.nivelNecesario + ".";
                    }
                }
            }
            return null;
        }

        public Collection<NPC> allNPCs() { return npcs.values(); }
    }

    /* ---------------------------
    FusionIndex: HashMaps en memoria
    --------------------------- */
    public static class indiceFusiones {
        private final Hash<String, String> indicePar = new Hash<>();

        private String keyNames(List<String> names) {
            List<String> s = new ArrayList<>(names);
            s.sort(String.CASE_INSENSITIVE_ORDER);
            return String.join("|", s);
        }

        public void construirDe(Collection<Persona> personas) {
        indicePar.clear();
            for (Persona p : personas) {
                if (p.posiblesFusiones == null) continue;
                for (FusionEntry fe : p.posiblesFusiones) {
                    List<String> nombres = new ArrayList<>();
                    nombres.add(p.nombre);
                    nombres.addAll(fe.con);
                    String k = keyNames(nombres);
                    indicePar.put(k, fe.resultado);
                }
            }
        }

        // Obtener el resultado de fusionar dos personas
        public String resultadoFusion(engine.Persona a, engine.Persona b) {
            String pairKey = keyNames(Arrays.asList(a.nombre, b.nombre));
            return indicePar.get(pairKey);
        }
    }

    /* ---------------------------
    TEAM: arreglo fijo con liberar persona
    --------------------------- */
    public static class Equipo {
        public final Persona[] miembros;
        private final int capacidad;

        public Equipo(int capacidad) {
            this.capacidad = capacidad;
            this.miembros = new Persona[capacidad];
        }

        // Añadir persona por referencia (si hay espacio)
        public boolean agregarPersona(Persona p) {
            for (int i = 0; i < capacidad; i++) {
                if (miembros[i] == null) { miembros[i] = p; return true; }
            }
            return false;
        }

        // Liberar (deshacerse) de una persona por nombre
        public boolean liberarPersona(String name) {
            for (int i = 0; i < capacidad; i++) {
                if (miembros[i] != null && miembros[i].nombre.equalsIgnoreCase(name)) {
                    miembros[i] = null;
                    return true;
                }
            }
            return false;
        }

        // Remover por referencia (usado en las fusiones)
        public boolean removerPersona(Persona p) {
            for (int i = 0; i < capacidad; i++) {
                if (miembros[i] == p) { miembros[i] = null; return true; }
            }
            return false;
        }

        public Persona[] getMiembros() { return miembros; }

        public int espaciosOcupados() {
            int c = 0;
            for (Persona p : miembros) if (p != null) c++;
            return c;
        }

        public boolean tieneEspacio() { return espaciosOcupados() < capacidad; }

        // Imprimir equipo
        public void mostrarEquipo() {
            System.out.println("Equipo (cap " + capacidad + "):");
            for (int i = 0; i < capacidad; i++) {
                System.out.println(" [" + i + "] " + (miembros[i] == null ? "<vacío>" : miembros[i]));
            }
        }
    }
    public class Arbol {

        private Nodo raiz;

        public Arbol() {
            raiz = null;
        }
    
        public class Nodo {

            int dato;

            Nodo izquierda;
            Nodo derecha;

            public Nodo(int dato) {
                this.dato = dato;
                izquierda = null;
                derecha = null;
            }

        }
        public void insertar(int dato) {
        raiz = insertarRec(raiz, dato);
    }

        private Nodo insertarRec(Nodo actual, int dato) {

            if (actual == null)
                return new Nodo(dato);

            if (dato < actual.dato)
                actual.izquierda = insertarRec(actual.izquierda, dato);

            else if (dato > actual.dato)
                actual.derecha = insertarRec(actual.derecha, dato);

            return actual;

        }
        public boolean buscar(int dato) {
    return buscarRec(raiz, dato);
}
        private boolean buscarRec(Nodo actual, int dato) {

            if (actual == null)
                return false;

            if (actual.dato == dato)
                return true;

            if (dato < actual.dato)
                return buscarRec(actual.izquierda, dato);

            return buscarRec(actual.derecha, dato);

        }

    }

    public static class MiHash<K, V> {

        public class Nodo<K, V> {

        K llave;
        V valor;

        Nodo<K, V> siguiente;

            public Nodo(K llave, V valor) {
                this.llave = llave;
                this.valor = valor;
                this.siguiente = null;
            }
        }

        private Nodo<K, V>[] tabla;
        private int tamaño;

        @SuppressWarnings("unchecked")
        public MiHash(int tamaño) {
            this.tamaño = tamaño;
            tabla = (Nodo<K, V>[]) new Nodo[tamaño];
    }

        private int hash(K llave) {
            return Math.abs(llave.hashCode()) % tamaño;
        }
    
        public void put(K llave, V valor) {

            int indice = hash(llave);

            Nodo<K, V> nuevo = new Nodo<>(llave, valor);

            if (tabla[indice] == null) {

                tabla[indice] = nuevo;
                return;

            }

            Nodo<K, V> actual = tabla[indice];

            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }

            actual.siguiente = nuevo;
        }

        public V get(K llave) {

            int indice = hash(llave);

            Nodo<K, V> actual = tabla[indice];

            while (actual != null) {

                if (actual.llave.equals(llave)) {
                    return actual.valor;
                }

                actual = actual.siguiente;

            }

            return null;

        }
        public boolean remove(K llave) {

            int indice = hash(llave);

            Nodo<K, V> actual = tabla[indice];
            Nodo<K, V> anterior = null;

            while (actual != null) {

                if (actual.llave.equals(llave)) {

                    if (anterior == null)
                        tabla[indice] = actual.siguiente;
                    else
                        anterior.siguiente = actual.siguiente;

                    return true;
                }

                anterior = actual;
                actual = actual.siguiente;
            }

            return false;
        }
    }
}
