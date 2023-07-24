package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.components.*;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ServerHandler extends DatabaseHandler {

    // Keys
    private final DisplayNameHandler DISPLAY_NAME;
    private final IdentifiersHandler IDENTIFIERS;
    private final SyncRulesHandler SYNC_RULES;
    private final AffixHandler AFFIX;

    public ServerHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
        DISPLAY_NAME = new DisplayNameHandler(mongoCollection);
        IDENTIFIERS = new IdentifiersHandler(mongoCollection, false, false);
        SYNC_RULES = new SyncRulesHandler(mongoCollection);
        AFFIX = new AffixHandler(mongoCollection);
    }

    public void register(Identifier serverIdentifier, String displayName, boolean displayNames, boolean roles, String prefix, String suffix) throws InvalidDisplayNameException, RecordAlreadyRegisteredException {
        // Throw an error if this identifier is registered
        if (exists(IDENTIFIERS.getFilter(serverIdentifier))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (DISPLAY_NAME.inUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document serverDocument = new Document();
        DISPLAY_NAME.writeDocument(serverDocument, displayName);
        IDENTIFIERS.writeDocument(serverDocument, serverIdentifier);
        SYNC_RULES.writeDocument(serverDocument, displayNames, roles);
        AFFIX.writeDocument(serverDocument, prefix, suffix);
        // Insert into database and return result
        insert(serverDocument).wasAcknowledged();
    }

    public void unregister(Identifier serverIdentifier) throws NotRegisteredException {
        if (!exists(IDENTIFIERS.getFilter(serverIdentifier))) throw new NotRegisteredException();
        // Insert into database and return result
        remove(getFilter(serverIdentifier)).wasAcknowledged();
    }

    public boolean setDisplayName(Bson filter, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        return DISPLAY_NAME.set(filter, newDisplayName);
    }

    public boolean doesSyncDisplayNames(Bson filter) {
        return SYNC_RULES.doesSyncDisplayNames(filter);
    }

    public boolean doesSyncRoles(Bson filter) {
        return SYNC_RULES.doesSyncRoles(filter);
    }

    public void setSyncDisplayNames(Bson filter, boolean value) {
        SYNC_RULES.setSyncDisplayNames(filter, value);
    }

    public void setSyncRoles(Bson filter, boolean value) {
        SYNC_RULES.setSyncRoles(filter, value);
    }

    public boolean setPrefix(Bson filter, String prefix) {
        return AFFIX.setPrefix(filter, prefix);
    }

    public boolean setSuffix(Bson filter, String suffix) {
        return AFFIX.setSuffix(filter, suffix);
    }

    public String[] getAffix(Bson filter) throws NotRegisteredException {
        return AFFIX.get(filter);
    }

    public Bson getFilter(String name) {
        return DISPLAY_NAME.getFilter(name);
    }

    public Bson getFilter(Identifier identifier) {
        return IDENTIFIERS.getFilter(identifier);
    }
}
