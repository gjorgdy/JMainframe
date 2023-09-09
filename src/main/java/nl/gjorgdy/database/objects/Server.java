package nl.gjorgdy.database.objects;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.objects.components.Affix;
import nl.gjorgdy.database.objects.components.Connection;
import nl.gjorgdy.database.objects.components.Connections;
import nl.gjorgdy.database.objects.components.DisplayName;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class Server extends DatabaseObject {

    static String DISPLAY_NAME = "display_name";
    public final DisplayName displayName;
    //static String CONNECTION = "connection";
    //public final Connection connection;
    public final Connections identifiers;
    static String AFFIX = "affix";
    public final Affix affix;

    public Server fromFilter(MongoCollection<Document> mongoCollection, Bson filter) throws NotRegisteredException {
        Document document = mongoCollection.find(filter).first();
        if (document == null) throw new NotRegisteredException();
        return new Server(mongoCollection, document.getObjectId("_id"));
    }

    public Server(MongoCollection<Document> mongoCollection, @NotNull ObjectId _id) throws NotRegisteredException {
        super(mongoCollection, _id, Mainframe.EVENT_HANDLERS.SERVERS);
        displayName = new DisplayName(this, DISPLAY_NAME);
        //connection = new Connection(this, CONNECTION);
        identifiers = new Connections(this, "identifiers");
        affix = new Affix(this, AFFIX);
    }

}
