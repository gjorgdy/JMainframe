package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.DisplayNameHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ServerHandler extends DisplayNameHandler {

    // Keys
    static final String SYNC = "sync";
    static final String DISPLAY_NAMES = "display_names";
    static final String ROLES = "roles";

    public ServerHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection, false, false);
    }

    public boolean register(Identifier connection, String displayName) throws InvalidDisplayNameException, RecordAlreadyRegisteredException {
        // Throw an error if this identifier is registered
        if (exists(getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document serverDocument = createDocument(connection, displayName)
            .append(SYNC, new Document(DISPLAY_NAMES, false).append(ROLES, false));
        // Insert into database and return result
        return insert(serverDocument).wasAcknowledged();
    }

    public boolean setDisplayName(Bson filter, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        return super.setDisplayName(filter, newDisplayName);
    }

    public boolean doesSyncDisplayNames(Bson filter) {
        Document serverDocument = findOne(filter);
        return serverDocument == null || serverDocument.get(SYNC, Document.class).getBoolean(DISPLAY_NAMES);
    }

    public boolean doesSyncRoles(Bson filter) {
        Document serverDocument = findOne(filter);
        return serverDocument == null || serverDocument.get(SYNC, Document.class).getBoolean(ROLES);
    }

    public boolean setSyncDisplayNames(Bson filter, boolean value) {
        return setValue(filter, (SYNC + "." + DISPLAY_NAMES), value).getModifiedCount() > 0;
    }

    public boolean setSyncRoles(Bson filter, boolean value) {
        return setValue(filter, (SYNC + "." + ROLES), value).getModifiedCount() > 0;
    }
}
