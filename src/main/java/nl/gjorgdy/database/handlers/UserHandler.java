package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.components.DatabaseHandler;
import nl.gjorgdy.database.handlers.components.DisplayNameHandler;
import nl.gjorgdy.database.handlers.components.IdentifiersHandler;
import nl.gjorgdy.database.handlers.components.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class UserHandler extends DatabaseHandler {

    // Keys
    static final String AVATAR_URL = "avatar_url"; // String
    static final String TIMESTAMP = "timestamp"; // Date
    private final DisplayNameHandler DISPLAY_NAME;
    private final IdentifiersHandler IDENTIFIERS;
    private final RolesHandler ROLES;

    public UserHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
        IDENTIFIERS = new IdentifiersHandler(mongoCollection, true, false);
        DISPLAY_NAME = new DisplayNameHandler(mongoCollection);
        ROLES = new RolesHandler(mongoCollection, IDENTIFIERS);
    }

    /**
     * Register a new user
     *
     * @param userIdentifier identifier of the user
     * @param displayName the name for the user
     */
    public boolean register(Identifier userIdentifier, String displayName) throws RecordAlreadyRegisteredException, InvalidDisplayNameException {
        // Throw an error if this identifier is registered
        if (exists(IDENTIFIERS.getFilter(userIdentifier))) throw new RecordAlreadyRegisteredException();
        // Create a new user
        Document userDocument = new Document(AVATAR_URL, "")
            .append(TIMESTAMP, new Date());
        DISPLAY_NAME.writeDocument(userDocument, displayName);
        IDENTIFIERS.writeDocument(userDocument, userIdentifier);
        ROLES.writeDocument(userDocument);
        // Insert into database
        InsertOneResult insertOneResult = insert(userDocument);
        // Send update
        List<Identifier> userIdentifiers = IDENTIFIERS.getAll(IDENTIFIERS.getFilter(userIdentifier));
        Mainframe.Events.onUserConnectionUpdate(userIdentifiers, userIdentifier, true);
        // Return result of insertion
        return insertOneResult.wasAcknowledged();
    }

    public boolean setDisplayName(Bson userFilter, String newDisplayName, String[] affix) throws InvalidDisplayNameException {
        String formattedDisplayName;
        String suffixRegex = " " + affix[1] + "$";
        formattedDisplayName = newDisplayName.replaceAll(suffixRegex, "");
        String prefixRegex = "^" + affix[0] + " ";
        formattedDisplayName = formattedDisplayName.replaceAll(prefixRegex, "");
        return setDisplayName(userFilter, formattedDisplayName, newDisplayName.equals(formattedDisplayName));
    }

    public boolean setDisplayName(Bson userFilter, String newDisplayName, Boolean forceEvent) throws InvalidDisplayNameException {
         try {
            if (DISPLAY_NAME.set(userFilter, newDisplayName) || forceEvent) {
                List<Identifier> userIdentifiers = IDENTIFIERS.getAll(userFilter);
                // Send event
                Mainframe.Events.onUserDisplayNameUpdate(userIdentifiers, newDisplayName);
                // Return result
                return true;
            }
         } catch (NotRegisteredException e) {
             System.err.println("Failed to set display name : " + e);
         }
        return false;
    }

    public String getDisplayName(Bson userFilter) throws NotRegisteredException {
        return DISPLAY_NAME.get(userFilter);
    }

    public boolean syncRoles(Bson userFilter, List<Identifier> blacklistRoleIdentifiers, List<Identifier> whitelistRoleIdentifiers) {
        try {
            boolean added = ROLES.add(userFilter, whitelistRoleIdentifiers);
            boolean removed = ROLES.remove(userFilter, blacklistRoleIdentifiers);
            // Execute update event if updated
            if (removed || added) {
                List<Identifier> userIdentifiers = IDENTIFIERS.getAll(userFilter);
                List<Identifier> roleIdentifiers = ROLES.getIdentifiers(userFilter);
                Mainframe.Events.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
                return true;
            }
        } catch (NotRegisteredException e) {
            System.err.println("Failed to add roles : " + e);
            System.err.println("User : " + userFilter.toBsonDocument().toJson());
        }
        return false;
    }

    public boolean addRole(Bson userFilter, Identifier roleIdentifier) {
        return addRoles(userFilter, List.of(roleIdentifier));
    }

    public boolean addRoles(Bson userFilter, List<Identifier> roleIdentifierList) {
        try {
            // Execute update event if updated
            if (ROLES.add(userFilter, roleIdentifierList)) {
                List<Identifier> userIdentifiers = IDENTIFIERS.getAll(userFilter);
                List<Identifier> roleIdentifiers = ROLES.getIdentifiers(userFilter);
                Mainframe.Events.onUserRoleUpdate(userIdentifiers, roleIdentifiers, true);
                return true;
            }
        } catch (NotRegisteredException e) {
            System.err.println("Failed to add roles : " + e);
            System.err.println("User : " + userFilter.toBsonDocument().toJson());
        }
        return false;
    }

    public boolean removeRole(Bson userFilter, Identifier roleIdentifier) {
        return removeRoles(userFilter, List.of(roleIdentifier));
    }

    public boolean removeRoles(Bson userFilter, List<Identifier> roleIdentifierList) {
        // Execute update event if updated
        try {
            if (ROLES.remove(userFilter, roleIdentifierList)) {
                List<Identifier> userIdentifiers = IDENTIFIERS.getAll(userFilter);
                List<Identifier> roleIdentifiers = ROLES.getIdentifiers(userFilter);
                Mainframe.Events.onUserRoleUpdate(userIdentifiers, roleIdentifiers, false);
                return true;
            }
        } catch (NotRegisteredException e) {
            System.err.println("Failed to remove roles : " + e);
            System.err.println("User : " + userFilter.toBsonDocument().toJson());
        }
        return false;
    }

    public boolean addLink(Bson userFilter, Identifier connectionIdentifier) throws RecordAlreadyConnectedException, RecordAlreadyRegisteredException {
        if (IDENTIFIERS.add(userFilter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> userIdentifiers = IDENTIFIERS.getAll(userFilter);
            Mainframe.Events.onUserConnectionUpdate(userIdentifiers, connectionIdentifier, true);
            return true;
        }
        return false;
    }

    public boolean removeLink(Bson filter, Identifier connectionIdentifier) {
        if (IDENTIFIERS.remove(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> userIdentifiers = IDENTIFIERS.getAll(filter);
            Mainframe.Events.onUserConnectionUpdate(userIdentifiers, connectionIdentifier, false);
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

    public List<Identifier> getRolesIdentifiers(Bson userFilter) throws NotRegisteredException {
        return ROLES.getIdentifiers(userFilter);
    }
}
