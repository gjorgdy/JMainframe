package nl.gjorgdy.database.codecs;

import nl.gjorgdy.database.records.Role;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class RoleArrayCodec implements Codec<Role[]> {

    @Override
    public Role[] decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartArray();
        List<Role> roles = new ArrayList<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            roles.add(new Role(reader.readObjectId(), "", null, null));
        }
        reader.readEndArray();
        return roles.toArray(new Role[0]);
    }

    @Override
    public void encode(BsonWriter writer, Role[] roles, EncoderContext encoderContext) {
        writer.writeStartArray();
        for (Role role : roles) {
            if (role._id() != null) writer.writeObjectId(role._id());
        }
        writer.writeEndArray();
    }

    @Override
    public Class<Role[]> getEncoderClass() {
        return Role[].class;
    }
}
