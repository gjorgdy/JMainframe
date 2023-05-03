package nl.gjorgdy.database.codecs;

import nl.gjorgdy.database.records.identifiers.Identifier;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;

public class IdentifierMapCodec implements Codec<Map<String, Identifier>> {

    private final Codec<Identifier> identifierCodec = new IdentifierCodec();

    @Override
    public Map<String, Identifier> decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartArray();
        Map<String, Identifier> identifiers = new HashMap<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            Identifier identifier = identifierCodec.decode(reader, decoderContext);
            identifiers.put(identifier.type().toString(), identifier);
        }
        reader.readEndArray();
        return identifiers;
    }

    @Override
    public void encode(BsonWriter writer, Map<String, Identifier> identifiers, EncoderContext encoderContext) {
        writer.writeStartArray();
        for (Identifier identifier : identifiers.values()) {
            identifierCodec.encode(writer, identifier, encoderContext);
        }
        writer.writeEndArray();
    }

    @Override
    public Class<Map<String, Identifier>> getEncoderClass() {
        return (Class<Map<String, Identifier>>) (Class<?>) Map.class;
    }
}
