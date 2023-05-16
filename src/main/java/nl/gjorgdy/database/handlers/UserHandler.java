package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.DisplayNameHandler;
import nl.gjorgdy.database.handlers.generic.IdentifiersHandler;
import nl.gjorgdy.database.handlers.generic.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;

import java.util.Date;

public class UserHandler extends RolesHandler {

    // Keys
    static final String AVATAR_URL = "avatar_url"; // String
    static final String TIMESTAMP = "timestamp"; // Date

    public UserHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection,true,false);
    }

    /**
     * Register a new user
     *
     * @param identifier identifier of the user
     * @param displayName the name for the user
     */
    public boolean register(Identifier identifier, String displayName) throws RecordAlreadyRegisteredException, InvalidDisplayNameException {
        // Throw an error if this identifier is registered
        if (exists(getFilter(identifier))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document userDocument = new Document(DisplayNameHandler.KEY, displayName);
        userDocument.append(IdentifiersHandler.KEY, new Identifier[0]);
        userDocument.append(AVATAR_URL, "");
        userDocument.append(TIMESTAMP, new Date());
        // Insert into database
        InsertOneResult insertOneResult = insert(userDocument);
        // Return result of insertion
        return insertOneResult.wasAcknowledged();
    }

    @Override
    public boolean setDisplayName(Identifier identifier, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        if (super.setDisplayName(identifier, newDisplayName)) {
            Identifier[] userIdentifiers = getAllIdentifiers(getFilter(identifier));
            // Send event
            Main.LISTENERS.onUserDisplayNameUpdate(userIdentifiers, newDisplayName);
            // Return result
            return true;
        }
        return false;
    }

    @Override
    public boolean addRole(Identifier userIdentifier, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (super.addRole(userIdentifier, roleIdentifier)) {
            Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRole(Identifier userIdentifier, Identifier roleIdentifier) throws NotRegisteredException {
        // Execute update event if updated
        if (super.removeRole(userIdentifier, roleIdentifier)) {
            Identifier[] userIdentifiers = getAllIdentifiers(getFilter(userIdentifier));
            Identifier[] roleIdentifiers = Main.MONGODB.roleHandler.getAllIdentifiers(getFilter(roleIdentifier));
            Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, false);
            return true;
        }
        return false;
    }

    public boolean addConnection(Identifier roleIdentifier, Identifier connectionIdentifier) throws NotRegisteredException, RecordAlreadyConnectedException, RecordAlreadyRegisteredException {
        if (super.addIdentifier(roleIdentifier, connectionIdentifier)) {
            // Execute update event
            Identifier[] roleIdentifiers = getAllIdentifiers(roleIdentifier);
            Main.LISTENERS.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, true);
            return true;
        }
        return false;
    }

    public boolean removeConnection(Identifier userIdentifier, Identifier connectionIdentifier) throws NotRegisteredException {
        if (super.removeIdentifier(userIdentifier, connectionIdentifier)) {
            // Execute update event
            Identifier[] userIdentifiers = getAllIdentifiers(userIdentifier);
            Main.LISTENERS.onUserConnectionUpdate(userIdentifiers, connectionIdentifier, false);
            return true;
        }
        return false;
    }

}
