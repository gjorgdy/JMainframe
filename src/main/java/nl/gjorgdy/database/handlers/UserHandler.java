package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Date;
import java.util.List;

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
     * @param userIdentifier identifier of the user
     * @param displayName the name for the user
     */
    public boolean register(Identifier userIdentifier, String displayName) throws RecordAlreadyRegisteredException, InvalidDisplayNameException {
        // Throw an error if this identifier is registered
        if (exists(getFilter(userIdentifier))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document userDocument = createDocument(userIdentifier, displayName)
            .append(AVATAR_URL, "")
            .append(TIMESTAMP, new Date());
        // Insert into database
        InsertOneResult insertOneResult = insert(userDocument);
        // Send update
        List<Identifier> userIdentifiers = getIdentifiers(getFilter(userIdentifier));
        Main.LISTENERS.onUserConnectionUpdate(userIdentifiers, userIdentifier, true);
        // Return result of insertion
        return insertOneResult.wasAcknowledged();
    }

    @Override
    public boolean setDisplayName(Bson filter, String newDisplayName) throws InvalidDisplayNameException {
         try {
            if (super.setDisplayName(filter, newDisplayName)) {
                List<Identifier> userIdentifiers = getIdentifiers(filter);
                // Send event
                Main.LISTENERS.onUserDisplayNameUpdate(userIdentifiers, newDisplayName);
                // Return result
                return true;
            }
         } catch (NotRegisteredException e) {
             System.err.println("Failed to set display name : " + e);
         }
        return false;
    }

    @Override
    public boolean addRole(Bson userFilter, Identifier roleIdentifier) {
        return addRoles(userFilter, List.of(roleIdentifier));
    }

    @Override
    public boolean addRoles(Bson userFilter, List<Identifier> roleIdentifierList) {
        try {
            // Execute update event if updated
            if (super.addRoles(userFilter, roleIdentifierList)) {
                List<Identifier> userIdentifiers = getIdentifiers(userFilter);
                List<Identifier> roleIdentifiers = getDocumentsRoleIdentifiers(userFilter);
                Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
                return true;
            }
        } catch (NotRegisteredException e) {
            System.err.println("Failed to add roles : " + e);
        }
        return false;
    }

    @Override
    public boolean removeRole(Bson userFilter, Identifier roleIdentifier) {
        return removeRoles(userFilter, List.of(roleIdentifier));
    }

    @Override
    public boolean removeRoles(Bson userFilter, List<Identifier> roleIdentifierList) {
        // Execute update event if updated
        try {
            if (super.removeRoles(userFilter, roleIdentifierList)) {
                List<Identifier> userIdentifiers = getIdentifiers(userFilter);
                List<Identifier> roleIdentifiers = getDocumentsRoleIdentifiers(userFilter);
                Main.LISTENERS.onUserRoleUpdate(userIdentifiers, roleIdentifiers, false);
                return true;
            }
        } catch (NotRegisteredException e) {
            System.err.println("Failed to remove roles : " + e);
            System.err.println("User : " + userFilter.toBsonDocument().toJson());
        }
        return false;
    }

    public boolean addConnection(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException, RecordAlreadyConnectedException, RecordAlreadyRegisteredException {
        if (super.addIdentifier(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> userIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onUserConnectionUpdate(userIdentifiers, connectionIdentifier, true);
            return true;
        }
        return false;
    }

    public boolean removeConnection(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException {
        if (super.removeIdentifier(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> userIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onUserConnectionUpdate(userIdentifiers, connectionIdentifier, false);
            return true;
        }
        return false;
    }

}
