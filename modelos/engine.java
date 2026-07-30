package modelos;

/**
 * Clase que contiene todas las estructuras de datos manuales implementadas
 * para el proyecto: Lista, Hash, Árbol, Grafo y MiHash.
 * 
 * @author Grupo 4 - Estructuras de Datos
 */
public class engine {

    // ============================================================
    // 1. LISTA ENLAZADA SIMPLE (Lista<T>)
    // ============================================================
    public static class Lista<T> implements java.lang.Iterable<T> {
        private static class Nodo<T> {
            T dato;
            Nodo<T> siguiente;
            Nodo(T dato) { this.dato = dato; }
        }
        private Nodo<T> cabeza;
        private int tamaño;

        public Lista() { cabeza = null; tamaño = 0; }

        public void add(T dato) {
            Nodo<T> nuevo = new Nodo<>(dato);
            if (cabeza == null) { cabeza = nuevo; }
            else {
                Nodo<T> actual = cabeza;
                while (actual.siguiente != null) actual = actual.siguiente;
                actual.siguiente = nuevo;
            }
            tamaño++;
        }

        public void clear() { cabeza = null; tamaño = 0; }

        public boolean contains(T dato) {
            Nodo<T> actual = cabeza;
            while (actual != null) {
                if (dato == null ? actual.dato == null : dato.equals(actual.dato)) return true;
                actual = actual.siguiente;
            }
            return false;
        }

        public T get(int index) {
            if (index < 0 || index >= tamaño) return null;
            Nodo<T> actual = cabeza;
            for (int i = 0; i < index; i++) actual = actual.siguiente;
            return actual.dato;
        }

        public boolean isEmpty() { return tamaño == 0; }

        public boolean remove(T dato) {
            if (cabeza == null) return false;
            if (dato == null ? cabeza.dato == null : dato.equals(cabeza.dato)) {
                cabeza = cabeza.siguiente;
                tamaño--;
                return true;
            }
            Nodo<T> anterior = cabeza;
            Nodo<T> actual = cabeza.siguiente;
            while (actual != null) {
                if (dato == null ? actual.dato == null : dato.equals(actual.dato)) {
                    anterior.siguiente = actual.siguiente;
                    tamaño--;
                    return true;
                }
                anterior = actual;
                actual = actual.siguiente;
            }
            return false;
        }

        public int size() { return tamaño; }

        public void sort() {
            if (tamaño <= 1) return;
            boolean cambio;
            do {
                cambio = false;
                Nodo<T> actual = cabeza;
                while (actual != null && actual.siguiente != null) {
                    if (actual.dato instanceof Comparable && actual.siguiente.dato instanceof Comparable) {
                        Comparable<T> a = (Comparable<T>) actual.dato;
                        Comparable<T> b = (Comparable<T>) actual.siguiente.dato;
                        if (a.compareTo((T) b) > 0) {
                            T temp = actual.dato;
                            actual.dato = actual.siguiente.dato;
                            actual.siguiente.dato = temp;
                            cambio = true;
                        }
                    }
                    actual = actual.siguiente;
                }
            } while (cambio);
        }

        @Override
        public java.util.Iterator<T> iterator() {
            return new java.util.Iterator<T>() {
                private Nodo<T> actual = cabeza;
                @Override public boolean hasNext() { return actual != null; }
                @Override public T next() { T valor = actual.dato; actual = actual.siguiente; return valor; }
            };
        }
    }

    // ============================================================
    // 2. TABLA HASH CON ENCADENAMIENTO (Hash<K, V>)
    // ============================================================
    public static class Hash<K, V> implements java.lang.Iterable<Hash.Entry<K, V>> {
        public static class Entry<K, V> {
            public K key;
            public V value;
            public Entry(K key, V value) { this.key = key; this.value = value; }
        }

        public interface ValueFactory<K, V> { V create(K key); }

        private static class Nodo<K, V> {
            K key;
            V value;
            Nodo<K, V> siguiente;
            Nodo(K key, V value) { this.key = key; this.value = value; }
        }

        private Nodo<K, V>[] tabla;
        private int capacidad;

        @SuppressWarnings("unchecked")
        public Hash() { this(16); }

        @SuppressWarnings("unchecked")
        public Hash(int capacidad) { this.capacidad = capacidad; this.tabla = (Nodo<K, V>[]) new Nodo[capacidad]; }

        private int hash(K key) { int codigo = key == null ? 0 : key.hashCode(); return Math.abs(codigo) % capacidad; }

        public void put(K key, V value) {
            int indice = hash(key);
            Nodo<K, V> actual = tabla[indice];
            while (actual != null) {
                if (key == null ? actual.key == null : key.equals(actual.key)) { actual.value = value; return; }
                actual = actual.siguiente;
            }
            Nodo<K, V> nuevo = new Nodo<>(key, value);
            nuevo.siguiente = tabla[indice];
            tabla[indice] = nuevo;
        }

        public V get(K key) {
            int indice = hash(key);
            Nodo<K, V> actual = tabla[indice];
            while (actual != null) {
                if (key == null ? actual.key == null : key.equals(actual.key)) return actual.value;
                actual = actual.siguiente;
            }
            return null;
        }

        public V getOrDefault(K key, V defecto) { V valor = get(key); return valor != null ? valor : defecto; }

        public boolean containsKey(K key) { return get(key) != null; }

        public V computeIfAbsent(K key, ValueFactory<K, V> factory) {
            V existente = get(key);
            if (existente != null) return existente;
            V creado = factory.create(key);
            put(key, creado);
            return creado;
        }

        public boolean remove(K key) {
            int indice = hash(key);
            Nodo<K, V> actual = tabla[indice];
            Nodo<K, V> anterior = null;
            while (actual != null) {
                if (key == null ? actual.key == null : key.equals(actual.key)) {
                    if (anterior == null) tabla[indice] = actual.siguiente;
                    else anterior.siguiente = actual.siguiente;
                    return true;
                }
                anterior = actual;
                actual = actual.siguiente;
            }
            return false;
        }

        public void clear() { for (int i = 0; i < capacidad; i++) tabla[i] = null; }

        public int size() {
            int cuenta = 0;
            for (int i = 0; i < capacidad; i++) {
                Nodo<K, V> actual = tabla[i];
                while (actual != null) { cuenta++; actual = actual.siguiente; }
            }
            return cuenta;
        }

        public boolean isEmpty() { return size() == 0; }

        public Lista<K> keySet() {
            Lista<K> salida = new Lista<>();
            for (int i = 0; i < capacidad; i++) {
                Nodo<K, V> actual = tabla[i];
                while (actual != null) { salida.add(actual.key); actual = actual.siguiente; }
            }
            return salida;
        }

        public Lista<V> values() {
            Lista<V> salida = new Lista<>();
            for (int i = 0; i < capacidad; i++) {
                Nodo<K, V> actual = tabla[i];
                while (actual != null) { salida.add(actual.value); actual = actual.siguiente; }
            }
            return salida;
        }

        public Lista<Entry<K, V>> entrySet() {
            Lista<Entry<K, V>> salida = new Lista<>();
            for (int i = 0; i < capacidad; i++) {
                Nodo<K, V> actual = tabla[i];
                while (actual != null) { salida.add(new Entry<>(actual.key, actual.value)); actual = actual.siguiente; }
            }
            return salida;
        }

        @Override
        public java.util.Iterator<Entry<K, V>> iterator() { return entrySet().iterator(); }
    }

    // ============================================================
    // 3. ÁRBOL BINARIO DE BÚSQUEDA (Arbol<K, V>)
    // ============================================================
    public static class Arbol<K, V> {
        public static <K, V> Arbol<K, V> crear() {
            return new Arbol<>();
        }

        public static class Entry<K, V> {
            public K key;
            public V value;
            public Entry(K key, V value) { this.key = key; this.value = value; }
        }

        public interface ValueFactory<K, V> { V create(K key); }

        private static class Nodo<K, V> {
            K key;
            V value;
            Nodo<K, V> izquierda;
            Nodo<K, V> derecha;
            Nodo(K key, V value) { this.key = key; this.value = value; }
        }

        private Nodo<K, V> raiz;
        private int tamaño;

        public Arbol() { raiz = null; tamaño = 0; }

        @SuppressWarnings("unchecked")
        private int comparar(K a, K b) {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            if (a instanceof Comparable && b instanceof Comparable) {
                return ((Comparable<Object>) a).compareTo(b);
            }
            return a.toString().compareTo(b.toString());
        }

        public void put(K key, V value) { raiz = insertar(raiz, key, value); }

        private Nodo<K, V> insertar(Nodo<K, V> actual, K key, V value) {
            if (actual == null) { tamaño++; return new Nodo<>(key, value); }
            int cmp = comparar(key, actual.key);
            if (cmp < 0) actual.izquierda = insertar(actual.izquierda, key, value);
            else if (cmp > 0) actual.derecha = insertar(actual.derecha, key, value);
            else actual.value = value;
            return actual;
        }

        public V get(K key) {
            Nodo<K, V> actual = raiz;
            while (actual != null) {
                int cmp = comparar(key, actual.key);
                if (cmp == 0) return actual.value;
                actual = cmp < 0 ? actual.izquierda : actual.derecha;
            }
            return null;
        }

        public V getOrDefault(K key, V defecto) {
            V valor = get(key);
            return valor != null ? valor : defecto;
        }

        public boolean containsKey(K key) { return get(key) != null; }

        public V computeIfAbsent(K key, ValueFactory<K, V> factory) {
            V existente = get(key);
            if (existente != null) return existente;
            V creado = factory.create(key);
            put(key, creado);
            return creado;
        }

        public void clear() { raiz = null; tamaño = 0; }

        public int size() { return tamaño; }

        public Lista<K> keySet() {
            Lista<K> salida = new Lista<>();
            recorridoInOrder(raiz, salida);
            return salida;
        }

        private void recorridoInOrder(Nodo<K, V> actual, Lista<K> salida) {
            if (actual == null) return;
            recorridoInOrder(actual.izquierda, salida);
            salida.add(actual.key);
            recorridoInOrder(actual.derecha, salida);
        }

        public Lista<V> values() {
            Lista<V> salida = new Lista<>();
            recorridoInOrderValores(raiz, salida);
            return salida;
        }

        private void recorridoInOrderValores(Nodo<K, V> actual, Lista<V> salida) {
            if (actual == null) return;
            recorridoInOrderValores(actual.izquierda, salida);
            salida.add(actual.value);
            recorridoInOrderValores(actual.derecha, salida);
        }

        public Lista<Entry<K, V>> entrySet() {
            Lista<Entry<K, V>> salida = new Lista<>();
            recorridoInOrderEntradas(raiz, salida);
            return salida;
        }

        private void recorridoInOrderEntradas(Nodo<K, V> actual, Lista<Entry<K, V>> salida) {
            if (actual == null) return;
            recorridoInOrderEntradas(actual.izquierda, salida);
            salida.add(new Entry<>(actual.key, actual.value));
            recorridoInOrderEntradas(actual.derecha, salida);
        }

        public Lista<K> descendingKeySet() {
            Lista<K> salida = new Lista<>();
            recorridoDescendente(raiz, salida);
            return salida;
        }

        private void recorridoDescendente(Nodo<K, V> actual, Lista<K> salida) {
            if (actual == null) return;
            recorridoDescendente(actual.derecha, salida);
            salida.add(actual.key);
            recorridoDescendente(actual.izquierda, salida);
        }

        public Lista<Entry<K, V>> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
            Lista<Entry<K, V>> salida = new Lista<>();
            recorrerRango(raiz, from, fromInclusive, to, toInclusive, salida);
            return salida;
        }

        private void recorrerRango(Nodo<K, V> actual, K from, boolean fromInclusive, K to, boolean toInclusive, Lista<Entry<K, V>> salida) {
            if (actual == null) return;
            int cmpFrom = comparar(actual.key, from);
            int cmpTo = comparar(actual.key, to);
            boolean dentro = true;
            if (from != null && ((cmpFrom < 0) || (!fromInclusive && cmpFrom == 0))) dentro = false;
            if (to != null && ((cmpTo > 0) || (!toInclusive && cmpTo == 0))) dentro = false;
            if (dentro) salida.add(new Entry<>(actual.key, actual.value));
            if (from == null || comparar(actual.key, from) > 0) recorrerRango(actual.izquierda, from, fromInclusive, to, toInclusive, salida);
            if (to == null || comparar(actual.key, to) < 0) recorrerRango(actual.derecha, from, fromInclusive, to, toInclusive, salida);
        }
    }

    // ============================================================
    // 4. MODELOS JSON / DOM
    // ============================================================
    public static class Persona {
        public String nombre;
        public String arcano;
        public int nivel;
        public Lista<FusionEntry> posiblesFusiones;
        public Lista<GeneratedByEntry> generadoPor;
        public Lista<specialEntry> fusionEspecial;
        public requiresEntry requisitoFusion;
        public Hash<String,Object> estadisticas;

        public Persona() {}
        @Override public String toString() { return nombre + " [" + arcano + " Lv:" + nivel + "]"; }
    }

    public static class FusionEntry {
        public Lista<String> con;
        public String resultado;
    }

    public static class GeneratedByEntry {
        public Lista<String> de;
    }

    public static class specialEntry {
        public Lista<String> de;
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
        public java.util.List<Requisito> requisitos = new java.util.ArrayList<>();

        public NPC() {}
        public NPC(String nombre, int nivelMaximo) {
            this.nombre = nombre;
            this.nivelMaximo = nivelMaximo;
            this.nivelActual = 0;
            this.requisitos = new java.util.ArrayList<>();
        }
    }

    public static class Requisito {
        public String npcDependiente;
        public int nivelNecesario;
        public int mesMinimo;
    }

    public static class SocialLinkData {
        public java.util.List<SocialLinkEntry> socialLinks;
    }

    public static class SocialLinkEntry {
        public String npc;
        public String arcano;
        public int nivelMaximo;
        public java.util.List<String> desbloquea;
        public java.util.List<Requisito> requisitos;
    }

    // ============================================================
    // 5. REGISTRO (Lista enlazada + índice Hash)
    // ============================================================
    public static class Registro {
        public static class Node {
            Persona dato;
            Node next;
            Node(Persona p) { dato = p; next = null; }
        }
        private Node cabeza;
        private int tamaño;
        private final Hash<String, Node> indicePorNombre = new Hash<>();

        public Registro() { cabeza = null; tamaño = 0; }

        public void buildFrom(Lista<Persona> personas) {
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

        public Node nodoPorNombre(String name) {
            return indicePorNombre.get(name.toLowerCase());
        }

        public Lista<Persona> toList() {
            Lista<Persona> out = new Lista<>();
            Node cur = cabeza;
            while (cur != null) { out.add(cur.dato); cur = cur.next; }
            return out;
        }

        public int size() { return tamaño; }

        public void mostrarRegistro() {
            Node cur = cabeza;
            while (cur != null) {
                System.out.println(cur.dato);
                cur = cur.next;
            }
        }
    }

    // ============================================================
    // 6. INDICE POR NIVEL (Usa Arbol)
    // ============================================================
    public static class indicePorNivel {
        private final Arbol<Integer, Lista<Persona>> indice = Arbol.crear();

        public void buildFrom(Lista<Persona> personas) {
            indice.clear();
            for (Persona p : personas) {
                indice.computeIfAbsent(p.nivel, k -> new Lista<>()).add(p);
            }
        }

        public Lista<Persona> busquedaPorRango(int min, int max) {
            Lista<Persona> out = new Lista<>();
            Lista<Arbol.Entry<Integer, Lista<Persona>>> sub = indice.subMap(min, true, max, true);
            for (Arbol.Entry<Integer, Lista<Persona>> entry : sub) {
                for (Persona p : entry.value) out.add(p);
            }
            return out;
        }

        public Lista<Persona> topN(int n) {
            Lista<Persona> out = new Lista<>();
            for (Integer lvl : indice.descendingKeySet()) {
                for (Persona p : indice.get(lvl)) {
                    out.add(p);
                    if (out.size() >= n) return out;
                }
            }
            return out;
        }
    }

    // ============================================================
    // 7. GRAFO DE SOCIAL LINKS (grafoSocialLinks)
    // ============================================================
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
            for (Hash.Entry<String,Integer> entry : socialLevels.entrySet()) {
                if (entry.key != null && entry.key.equalsIgnoreCase(nombreNPC)) return entry.value;
            }
            return 0;
        }

        public void construirDe(Lista<NPC> npcList) {
            npcs.clear();
            for (NPC n : npcList) {
                if (n == null || n.nombre == null) continue;
                npcs.put(n.nombre, n);
            }
        }

        public void construirDesdePersonas(Lista<Persona> personas) {
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

        public void construirDesdeSocialLinks(java.util.List<SocialLinkEntry> entries) {
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
                npc.requisitos = entry.requisitos == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(entry.requisitos);
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
            Lista<String> partes = new Lista<>();
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

        public boolean isFusionUnlocked(Persona resultado, modelos.usuario.User usuario) {
            if (resultado == null || resultado.requisitoFusion == null || resultado.requisitoFusion.socialLink == null || resultado.requisitoFusion.socialLink.isBlank()) {
                return true;
            }
            String npcName = resultado.requisitoFusion.socialLink;
            int level = usuario.getSocialLinkLevel(npcName);
            int required = getRequiredLevel(npcName);
            return level >= required;
        }

        public void ensureNPCsFor(Lista<String> nombres) {
            if (nombres == null) return;
            for (String nombre : nombres) {
                ensureNPC(nombre);
            }
        }

        public void syncLevels(Hash<String,Integer> socialLevels) {
            if (socialLevels == null) return;
            for (Hash.Entry<String,Integer> entry : socialLevels.entrySet()) {
                String npcName = entry.key;
                int level = entry.value == null ? 0 : entry.value;
                String canonical = resolveName(npcName);
                ensureNPC(canonical);
                NPC npc = npcs.get(canonical);
                if (npc != null) {
                    npc.nivelActual = Math.max(0, Math.min(npc.nivelMaximo, level));
                }
            }
        }

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
                return null;
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

        public Lista<NPC> allNPCs() { return npcs.values(); }
    }

    // ============================================================
    // 8. INDICE DE FUSIONES (Usa Hash)
    // ============================================================
    public static class indiceFusiones {
        private final Hash<String, String> indicePar = new Hash<>();

        private String keyNames(Lista<String> names) {
            Lista<String> s = new Lista<>();
            for (String name : names) s.add(name);
            s.sort();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.size(); i++) {
                if (i > 0) sb.append('|');
                sb.append(s.get(i));
            }
            return sb.toString();
        }

        public void construirDe(Lista<Persona> personas) {
            indicePar.clear();
            for (Persona p : personas) {
                if (p.posiblesFusiones == null) continue;
                for (FusionEntry fe : p.posiblesFusiones) {
                    Lista<String> nombres = new Lista<>();
                    nombres.add(p.nombre);
                    for (String nombre : fe.con) nombres.add(nombre);
                    String k = keyNames(nombres);
                    indicePar.put(k, fe.resultado);
                }
            }
        }

        public String resultadoFusion(engine.Persona a, engine.Persona b) {
            Lista<String> names = new Lista<>();
            names.add(a.nombre);
            names.add(b.nombre);
            String pairKey = keyNames(names);
            return indicePar.get(pairKey);
        }
    }

    // ============================================================
    // 9. EQUIPO (Arreglo fijo)
    // ============================================================
    public static class Equipo {
        public final Persona[] miembros;
        private final int capacidad;

        public Equipo(int capacidad) {
            this.capacidad = capacidad;
            this.miembros = new Persona[capacidad];
        }

        public boolean agregarPersona(Persona p) {
            for (int i = 0; i < capacidad; i++) {
                if (miembros[i] == null) { miembros[i] = p; return true; }
            }
            return false;
        }

        public boolean liberarPersona(String name) {
            for (int i = 0; i < capacidad; i++) {
                if (miembros[i] != null && miembros[i].nombre.equalsIgnoreCase(name)) {
                    miembros[i] = null;
                    return true;
                }
            }
            return false;
        }

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

        public void mostrarEquipo() {
            System.out.println("Equipo (cap " + capacidad + "):");
            for (int i = 0; i < capacidad; i++) {
                System.out.println(" [" + i + "] " + (miembros[i] == null ? "<vacío>" : miembros[i]));
            }
        }
    }

    // ============================================================
    // 10. ÁRBOL BINARIO SIMPLE (ArbolSimple)
    // ============================================================
    public static class ArbolSimple {
        private Nodo raiz;

        public ArbolSimple() { raiz = null; }

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

        public void insertar(int dato) { raiz = insertarRec(raiz, dato); }

        private Nodo insertarRec(Nodo actual, int dato) {
            if (actual == null) return new Nodo(dato);
            if (dato < actual.dato) actual.izquierda = insertarRec(actual.izquierda, dato);
            else if (dato > actual.dato) actual.derecha = insertarRec(actual.derecha, dato);
            return actual;
        }

        public boolean buscar(int dato) { return buscarRec(raiz, dato); }

        private boolean buscarRec(Nodo actual, int dato) {
            if (actual == null) return false;
            if (actual.dato == dato) return true;
            if (dato < actual.dato) return buscarRec(actual.izquierda, dato);
            return buscarRec(actual.derecha, dato);
        }
    }

    // ============================================================
    // 11. TABLA HASH SIMPLE (MiHash<K, V>)
    // ============================================================
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

        private int hash(K llave) { return Math.abs(llave.hashCode()) % tamaño; }

        public void put(K llave, V valor) {
            int indice = hash(llave);
            Nodo<K, V> nuevo = new Nodo<>(llave, valor);
            if (tabla[indice] == null) {
                tabla[indice] = nuevo;
                return;
            }
            Nodo<K, V> actual = tabla[indice];
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }

        public V get(K llave) {
            int indice = hash(llave);
            Nodo<K, V> actual = tabla[indice];
            while (actual != null) {
                if (actual.llave.equals(llave)) return actual.valor;
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
                    if (anterior == null) tabla[indice] = actual.siguiente;
                    else anterior.siguiente = actual.siguiente;
                    return true;
                }
                anterior = actual;
                actual = actual.siguiente;
            }
            return false;
        }
    }
}