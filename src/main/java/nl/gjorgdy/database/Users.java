package nl.gjorgdy.database;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.Main;
import nl.gjorgdy.objects.Identifier;
import nl.gjorgdy.objects.Role;
import nl.gjorgdy.objects.User;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Users implements Codec<User> {

    private final MongoCollection<User> collection;

    public Users(MongoCollection<User> collection) {
        this.collection = collection;
    }

    @Override
    public User decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        // Read user id
        ObjectId id = reader.readObjectId("_id");
        // Read display name
        String displayName = reader.readString("display_name");
        // Read roles
        List<Role> roles = new ArrayList<>();
        reader.readName("roles");
        reader.readStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            roles.add(Main.MONGO_DB.getRole(reader.readObjectId()));
        }
        reader.readEndArray();
        // Read connections

        return new User(id, displayName, roles, new HashMap<>());
    }

    @Override
    public void encode(BsonWriter writer, User value, EncoderContext encoderContext) {

    }

    @Override
    public Class<User> getEncoderClass() {
        return null;
    }

    public User get(ObjectId id) {
        return collection.find(new Document("_id", id)).first();
    }
}
