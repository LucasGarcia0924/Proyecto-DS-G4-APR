package modelos;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import modelos.engine.Hash;

/**
 * Módulo Jackson para serializar/deserializar engine.Hash<String,Object>
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class HashModule extends SimpleModule {

    public HashModule() {
        // Deserializador
        addDeserializer(Hash.class, new JsonDeserializer() {
            @Override
            public Hash deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                ObjectMapper mapper = (ObjectMapper) p.getCodec();
                JsonNode node = p.readValueAsTree();
                Hash out = new Hash();
                if (node != null && node.isObject()) {
                    ObjectNode obj = (ObjectNode) node;
                    java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = obj.fields();
                    while (it.hasNext()) {
                        java.util.Map.Entry<String, JsonNode> e = it.next();
                        String key = e.getKey();
                        JsonNode valNode = e.getValue();
                        Object value;
                        if (valNode.isInt()) value = valNode.asInt();
                        else if (valNode.isLong()) value = valNode.asLong();
                        else if (valNode.isDouble()) value = valNode.asDouble();
                        else if (valNode.isBoolean()) value = valNode.asBoolean();
                        else if (valNode.isTextual()) value = valNode.asText();
                        else if (valNode.isObject() || valNode.isArray()) {
                            // convertir estructuras complejas a Map/List usando mapper
                            value = mapper.treeToValue(valNode, Object.class);
                        } else if (valNode.isNull()) value = null;
                        else value = mapper.treeToValue(valNode, Object.class);
                        out.put(key, value);
                    }
                }
                return out;
            }
        });

        // Serializador
        JsonSerializer hashSerializer = new JsonSerializer<Hash>() {
            @Override
            public void serialize(Hash value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                if (value != null) {
                    for (Hash.Entry entry : value.entrySet()) {
                        String k = (String) entry.key;
                        Object v = entry.value;
                        gen.writeFieldName(k);
                        // delegar a Jackson para serializar el valor
                        gen.writeObject(v);
                    }
                }
                gen.writeEndObject();
            }
        };

        addSerializer((Class) Hash.class, hashSerializer);
    }
}
