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

public class StringArrayCodec implements Codec<String[]> {
    private final Codec<Identifier> identifierCodec = new IdentifierCodec();

    @Override
    public String[] decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartArray();
        List<String> strings = new ArrayList<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String string = reader.readString();
            strings.add(string);
        }
        reader.readEndArray();
        return strings.toArray(new String[0]);
    }

    @Override
    public void encode(BsonWriter writer, String[] strings, EncoderContext encoderContext) {
        writer.writeStartArray();
        for (String string : strings) {
            writer.writeString(string);
        }
        writer.writeEndArray();
    }

    @Override
    public Class<String[]> getEncoderClass() {
        return String[].class;
    }
}
