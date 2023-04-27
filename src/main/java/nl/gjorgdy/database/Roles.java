package nl.gjorgdy.database;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.objects.Identifier;
import nl.gjorgdy.objects.Role;
import nl.gjorgdy.objects.User;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Roles {

    private MongoCollection<Role> collection;

    public Roles(MongoCollection<Role> collection) {
        this.collection = collection;
    }

    public Role get(ObjectId id) {
        return collection.find(new Document("_id", id)).first();
    }

    public boolean insert(Role object) {
        return false;
    }
}
