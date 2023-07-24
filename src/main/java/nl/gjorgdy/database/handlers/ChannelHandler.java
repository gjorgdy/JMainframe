package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.handlers.components.DatabaseHandler;
import nl.gjorgdy.database.handlers.components.DisplayNameHandler;
import nl.gjorgdy.database.handlers.components.IdentifiersHandler;
import nl.gjorgdy.database.handlers.components.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ChannelHandler extends DatabaseHandler {

    private final DisplayNameHandler DISPLAY_NAME;
    private final IdentifiersHandler IDENTIFIERS;
    private final RolesHandler ALLOWED_ROLES;

    public ChannelHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
        IDENTIFIERS = new IdentifiersHandler(mongoCollection, true, false);
        DISPLAY_NAME = new DisplayNameHandler(mongoCollection);
        ALLOWED_ROLES = new RolesHandler(mongoCollection, IDENTIFIERS);
    }

    public boolean register(String displayName) throws InvalidDisplayNameException {
        if (DISPLAY_NAME.inUse(displayName)) throw new InvalidDisplayNameException();
        // Create channel record
        Document channelDocument = new Document();
        DISPLAY_NAME.writeDocument(channelDocument, displayName);
        IDENTIFIERS.writeDocument(channelDocument);
        ALLOWED_ROLES.writeDocument(channelDocument);
        // Return
        return insert(channelDocument).wasAcknowledged();
    }

    public boolean addAllowedRole(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (ALLOWED_ROLES.add(filter, roleIdentifier)) {
            //Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            //Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            //Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
            return true;
        }
        return false;
    }

    public boolean removeAllowedRole(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (ALLOWED_ROLES.remove(filter, roleIdentifier)) {
            //Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            //Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            //Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, false);
            return true;
        }
        return false;
    }

    public Bson getFilter(String name) {
        return DISPLAY_NAME.getFilter(name);
    }

    public Bson getFilter(Identifier identifier) {
        return IDENTIFIERS.getFilter(identifier);
    }

}
