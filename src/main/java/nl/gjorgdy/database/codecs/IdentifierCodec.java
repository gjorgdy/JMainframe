package nl.gjorgdy.database.codecs;

import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.LongIdentifier;
import nl.gjorgdy.database.records.identifiers.StringIdentifier;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class IdentifierCodec implements Codec<Identifier> {

    @Override
    public Identifier decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        // Get the type of the identifier
        BsonType bsonType = reader.readBsonType();
        // Read key
        Identifier.Types type = Identifier.Types.valueOf(reader.readName());
        // Read value
        Identifier id = null;
        if (bsonType == BsonType.INT64) {
            id = new LongIdentifier(type, reader.readInt64());
        } else if (bsonType == BsonType.STRING) {
            id = new StringIdentifier(type, reader.readString());
        }
        // End reading document
        reader.readEndDocument();
        // Return value
        return id;
    }

    @Override
    public void encode(BsonWriter writer, Identifier identifier, EncoderContext encoderContext) {
        writer.writeStartDocument();
        // Write the key
        writer.writeName(identifier.type().toString());
        // Write the value
        if (identifier instanceof LongIdentifier) {
            writer.writeInt64((long) identifier.id());
        } else if (identifier instanceof StringIdentifier) {
            writer.writeString((String) identifier.id());
        }
        // Stop writing the document
        writer.writeEndDocument();
    }

    @Override
    public Class<Identifier> getEncoderClass() {
        return Identifier.class;
    }
}
