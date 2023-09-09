package nl.gjorgdy.database.objects;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.objects.components.Affix;
import nl.gjorgdy.database.objects.components.Connections;
import nl.gjorgdy.database.objects.components.DisplayName;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class User extends DatabaseObject {

    static String DISPLAY_NAME = "display_name";
    public final DisplayName displayName;
    //static String CONNECTION = "connection";
    //public final Connection connection;
    public final Connections identifiers;

    public User fromFilter(MongoCollection<Document> mongoCollection, Bson filter) throws NotRegisteredException {
        Document document = mongoCollection.find(filter).first();
        if (document == null) throw new NotRegisteredException();
        return new User(mongoCollection, document.getObjectId("_id"));
    }

    public User(MongoCollection<Document> mongoCollection, @NotNull ObjectId _id) throws NotRegisteredException {
        super(mongoCollection, _id, Mainframe.EVENT_HANDLERS.USERS);
        displayName = new DisplayName(this, DISPLAY_NAME);
        //connection = new Connection(this, CONNECTION);
        identifiers = new Connections(this, "identifiers");
    }

}
