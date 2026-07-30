package modelos;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;

import modelos.engine.Lista;

@SuppressWarnings({"rawtypes","unchecked"})
public class ListaModule extends SimpleModule {

    public ListaModule() {
        // registrar deserializador contextual
        addDeserializer(Lista.class, new ListaDeserializer());

        // serializador genérico para Lista<T>
        JsonSerializer listaSerializer = new JsonSerializer<Lista<?>>() {
            @Override
            public void serialize(Lista<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartArray();
                if (value != null) {
                    for (Object item : value) {
                        gen.writeObject(item);
                    }
                }
                gen.writeEndArray();
            }
        };
        addSerializer((Class) Lista.class, listaSerializer);
    }

    // Deserializador contextual que detecta el tipo contenido (T) y lo usa para treeToValue
    public static class ListaDeserializer extends JsonDeserializer<Lista<?>> implements ContextualDeserializer {

        private JavaType contentType; // puede ser null (fallback a Object)

        public ListaDeserializer() { this.contentType = null; }

        public ListaDeserializer(JavaType contentType) { this.contentType = contentType; }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType ct = null;
            // 1) intentar obtener tipo desde la propiedad (ej. Lista<FusionEntry>)
            if (property != null) {
                JavaType propType = property.getType();
                if (propType != null && propType.hasGenericTypes()) {
                    ct = propType.containedType(0);
                }
            }
            // 2) fallback: intentar con el tipo contextual del contexto
            if (ct == null) {
                JavaType ctxType = ctxt.getContextualType();
                if (ctxType != null && ctxType.hasGenericTypes()) {
                    ct = ctxType.containedType(0);
                }
            }
            return new ListaDeserializer(ct);
        }

        @Override
        public Lista<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = p.readValueAsTree();
            Lista out = new Lista();
            if (node != null && node.isArray()) {
                ArrayNode arr = (ArrayNode) node;
                for (JsonNode item : arr) {
                    Object obj;
                    if (contentType != null) {
                        obj = mapper.treeToValue(item, mapper.getTypeFactory().constructType(contentType));
                    } else {
                        obj = mapper.treeToValue(item, Object.class);
                    }
                    out.add(obj);
                }
            }
            return out;
        }
    }
}
