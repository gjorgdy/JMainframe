package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.handlers.generic.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ChannelHandler extends RolesHandler {
    public ChannelHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection, true, true);
    }

    public boolean register(String displayName) throws InvalidDisplayNameException {
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create channel record
        Document channelDocument = createDocument(displayName);
        // Return
        return insert(channelDocument).wasAcknowledged();
    }

    public boolean addAllowedRole(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (super.addRole(filter, roleIdentifier)) {
            //Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            //Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            //Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
            return true;
        }
        return false;
    }

    public boolean removeAllowedRole(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (super.removeRole(filter, roleIdentifier)) {
            //Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            //Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            //Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, false);
            return true;
        }
        return false;
    }

}
