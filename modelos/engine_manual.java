package modelos;

public class engine_manual {

    public static class Lista<T> {
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
    }

    public static class Hash<K, V> {
        public static class Entry<K, V> { public K key; public V value; public Entry(K key, V value) { this.key = key; this.value = value; } }
        public interface ValueFactory<K, V> { V create(K key); }
        private static class Nodo<K, V> { K key; V value; Nodo<K, V> siguiente; Nodo(K key, V value){ this.key=key; this.value=value; } }
        private Nodo<K, V>[] tabla; private int tamaño;
        @SuppressWarnings("unchecked") public Hash(){ this(16);} @SuppressWarnings("unchecked") public Hash(int capacidad){ this.tamaño=capacidad; tabla=(Nodo<K,V>[])new Nodo[capacidad]; }
        private int hash(K key){ int codigo = key == null ? 0 : key.hashCode(); return Math.abs(codigo) % tamaño; }
        public void put(K key, V value){ int indice=hash(key); Nodo<K,V> actual=tabla[indice]; while(actual!=null){ if(key==null?actual.key==null:key.equals(actual.key)){ actual.value=value; return; } actual=actual.siguiente; } Nodo<K,V> nuevo=new Nodo<>(key,value); nuevo.siguiente=tabla[indice]; tabla[indice]=nuevo; }
        public V get(K key){ int indice=hash(key); Nodo<K,V> actual=tabla[indice]; while(actual!=null){ if(key==null?actual.key==null:key.equals(actual.key)) return actual.value; actual=actual.siguiente; } return null; }
        public V getOrDefault(K key, V defecto){ V valor=get(key); return valor != null ? valor : defecto; }
        public boolean containsKey(K key){ return get(key) != null; }
        public V computeIfAbsent(K key, ValueFactory<K,V> factory){ V existente=get(key); if(existente!=null) return existente; V creado=factory.create(key); put(key,creado); return creado; }
        public boolean remove(K key){ int indice=hash(key); Nodo<K,V> actual=tabla[indice]; Nodo<K,V> anterior=null; while(actual!=null){ if(key==null?actual.key==null:key.equals(actual.key)){ if(anterior==null) tabla[indice]=actual.siguiente; else anterior.siguiente=actual.siguiente; return true; } anterior=actual; actual=actual.siguiente; } return false; }
        public void clear(){ for(int i=0;i<tamaño;i++) tabla[i]=null; }
        public int size(){ int cuenta=0; for(int i=0;i<tamaño;i++){ Nodo<K,V> actual=tabla[i]; while(actual!=null){ cuenta++; actual=actual.siguiente; } } return cuenta; }
        public boolean isEmpty(){ return size()==0; }
        public Lista<K> keySet(){ Lista<K> salida=new Lista<>(); for(int i=0;i<tamaño;i++){ Nodo<K,V> actual=tabla[i]; while(actual!=null){ salida.add(actual.key); actual=actual.siguiente; } } return salida; }
        public Lista<V> values(){ Lista<V> salida=new Lista<>(); for(int i=0;i<tamaño;i++){ Nodo<K,V> actual=tabla[i]; while(actual!=null){ salida.add(actual.value); actual=actual.siguiente; } } return salida; }
        public Lista<Entry<K,V>> entrySet(){ Lista<Entry<K,V>> salida=new Lista<>(); for(int i=0;i<tamaño;i++){ Nodo<K,V> actual=tabla[i]; while(actual!=null){ salida.add(new Entry<>(actual.key, actual.value)); actual=actual.siguiente; } } return salida; }
    }

    public static class Arbol<K, V> {
        public static class Entrada<K, V> { public K key; public V value; public Entrada(K key, V value){ this.key=key; this.value=value; } }
        public interface ValueFactory<K,V>{ V create(K key); }
        private static class Nodo<K,V>{ K key; V value; Nodo<K,V> izquierda; Nodo<K,V> derecha; Nodo(K key, V value){ this.key=key; this.value=value; } }
        private Nodo<K,V> raiz; private int tamaño;
        public Arbol(){ raiz=null; tamaño=0; }
        @SuppressWarnings("unchecked") private int comparar(K a, K b){ if(a==null&&b==null)return 0; if(a==null)return -1; if(b==null)return 1; if(a instanceof Comparable && b instanceof Comparable) return ((Comparable<Object>)a).compareTo(b); return a.toString().compareTo(b.toString()); }
        public void put(K key, V value){ raiz=insertar(raiz,key,value); }
        private Nodo<K,V> insertar(Nodo<K,V> actual, K key, V value){ if(actual==null){ tamaño++; return new Nodo<>(key,value);} int cmp=comparar(key,actual.key); if(cmp<0) actual.izquierda=insertar(actual.izquierda,key,value); else if(cmp>0) actual.derecha=insertar(actual.derecha,key,value); else actual.value=value; return actual; }
        public V get(K key){ Nodo<K,V> actual=raiz; while(actual!=null){ int cmp=comparar(key,actual.key); if(cmp==0)return actual.value; actual=cmp<0?actual.izquierda:actual.derecha; } return null; }
        public V getOrDefault(K key, V defecto){ V valor=get(key); return valor!=null?valor:defecto; }
        public boolean containsKey(K key){ return get(key)!=null; }
        public V computeIfAbsent(K key, ValueFactory<K,V> factory){ V existente=get(key); if(existente!=null)return existente; V creado=factory.create(key); put(key,creado); return creado; }
        public void clear(){ raiz=null; tamaño=0; }
        public int size(){ return tamaño; }
        public Lista<K> keySet(){ Lista<K> salida=new Lista<>(); recorridoInOrder(raiz,salida); return salida; }
        private void recorridoInOrder(Nodo<K,V> actual, Lista<K> salida){ if(actual==null)return; recorridoInOrder(actual.izquierda,salida); salida.add(actual.key); recorridoInOrder(actual.derecha,salida); }
        public Lista<V> values(){ Lista<V> salida=new Lista<>(); recorridoInOrderValores(raiz,salida); return salida; }
        private void recorridoInOrderValores(Nodo<K,V> actual, Lista<V> salida){ if(actual==null)return; recorridoInOrderValores(actual.izquierda,salida); salida.add(actual.value); recorridoInOrderValores(actual.derecha,salida); }
        public Lista<Entrada<K,V>> entrySet(){ Lista<Entrada<K,V>> salida=new Lista<>(); recorridoInOrderEntradas(raiz,salida); return salida; }
        private void recorridoInOrderEntradas(Nodo<K,V> actual, Lista<Entrada<K,V>> salida){ if(actual==null)return; recorridoInOrderEntradas(actual.izquierda,salida); salida.add(new Entrada<>(actual.key, actual.value)); recorridoInOrderEntradas(actual.derecha,salida); }
        public Lista<K> descendingKeySet(){ Lista<K> salida=new Lista<>(); recorridoDescendente(raiz,salida); return salida; }
        private void recorridoDescendente(Nodo<K,V> actual, Lista<K> salida){ if(actual==null)return; recorridoDescendente(actual.derecha,salida); salida.add(actual.key); recorridoDescendente(actual.izquierda,salida); }
        public Lista<Entrada<K,V>> subMap(K from, boolean fromInclusive, K to, boolean toInclusive){ Lista<Entrada<K,V>> salida=new Lista<>(); recorrerRango(raiz, from, fromInclusive, to, toInclusive, salida); return salida; }
        private void recorrerRango(Nodo<K,V> actual, K from, boolean fromInclusive, K to, boolean toInclusive, Lista<Entrada<K,V>> salida){ if(actual==null)return; int cmpFrom=comparar(actual.key, from); int cmpTo=comparar(actual.key, to); boolean dentro=true; if(from!=null && ((cmpFrom<0)||(!fromInclusive&&cmpFrom==0))) dentro=false; if(to!=null && ((cmpTo>0)||(!toInclusive&&cmpTo==0))) dentro=false; if(dentro) salida.add(new Entrada<>(actual.key,actual.value)); if(from==null||comparar(actual.key,from)>0) recorrerRango(actual.izquierda,from,fromInclusive,to,toInclusive,salida); if(to==null||comparar(actual.key,to)<0) recorrerRango(actual.derecha,from,fromInclusive,to,toInclusive,salida); }
    }
}
