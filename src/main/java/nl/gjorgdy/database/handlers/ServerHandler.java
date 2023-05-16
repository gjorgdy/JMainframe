package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.DisplayNameHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;

public class ServerHandler extends DisplayNameHandler {

    // Keys
    static final String SYNC_DISPLAY_NAMES = "sync.display_names";
    static final String SYNC_ROLES = "sync.roles";

    public ServerHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection, false, false);
    }

    public boolean register(Identifier connection, String displayName) throws InvalidDisplayNameException, RecordAlreadyRegisteredException {
        // Throw an error if this identifier is registered
        if (exists(getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document serverDocument = createDocument(displayName)
        .append(SYNC_DISPLAY_NAMES, false)
        .append(SYNC_ROLES, false);
        // Insert into database and return result
        return insert(serverDocument).wasAcknowledged();
    }

    public boolean setDisplayName(Identifier identifier, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        return super.setDisplayName(identifier, newDisplayName);
    }

    public boolean setSyncDisplayNames(Identifier identifier, boolean value) {
        return setValue(getFilter(identifier), SYNC_DISPLAY_NAMES, value).getModifiedCount() > 0;
    }

    public boolean setSyncRoles(Identifier identifier, boolean value) {
        return setValue(getFilter(identifier), SYNC_ROLES, value).getModifiedCount() > 0;
    }
}
