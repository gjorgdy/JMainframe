package nl.gjorgdy.database.codecs;

import nl.gjorgdy.database.records.identifiers.Identifier;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class IdentifierArrayCodec implements Codec<Identifier[]> {
    private final Codec<Identifier> identifierCodec = new IdentifierCodec();

    @Override
    public Identifier[] decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartArray();
        List<Identifier> identifiers = new ArrayList<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            Identifier identifier = identifierCodec.decode(reader, decoderContext);
            identifiers.add(identifier);
        }
        reader.readEndArray();
        return identifiers.toArray(new Identifier[0]);
    }

    @Override
    public void encode(BsonWriter writer, Identifier[] identifiers, EncoderContext encoderContext) {
        writer.writeStartArray();
        for (Identifier identifier : identifiers) {
            identifierCodec.encode(writer, identifier, encoderContext);
        }
        writer.writeEndArray();
    }

    @Override
    public Class<Identifier[]> getEncoderClass() {
        return Identifier[].class;
    }
}
