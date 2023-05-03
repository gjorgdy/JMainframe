package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.UserAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.UserAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.DisplayNameHandler;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.UserRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

import java.util.Date;
import java.util.Map;

public class UserHandler extends DisplayNameHandler<UserRecord> {

    public UserHandler(MongoCollection<UserRecord> mongoCollection) {
        super(mongoCollection);
    }

    /**
     * Create a new user
     *
     * @param identifier identifier of the user
     * @param displayName the name for the user
     */
    public UserRecord register(Identifier identifier, String displayName) throws UserAlreadyRegisteredException, InvalidDisplayNameException {
        // Check if this identifier is registered
        UserRecord user = get(identifier);
        if (get(identifier) != null) {
            System.err.println(user.toFormattedString());
            throw new UserAlreadyRegisteredException();
        }
        // Check if display name is already in use
        if (isDisplayNameUsed(displayName)) {
            throw new InvalidDisplayNameException();
        }
        // Create a new user
        UserRecord userRecord = new UserRecord(null, displayName, new Identifier[0], Map.of(identifier.type().toString(), identifier), new Date());
        // Insert into database
        insert(userRecord);
        // Return object
        return userRecord;
    }

    @Override
    public UserRecord setDisplayName(UserRecord userRecord, String newDisplayName) throws InvalidDisplayNameException {
        UserRecord updatedUserRecord = super.setDisplayName(userRecord, newDisplayName);
        // Execute update event
        Main.LISTENERS.onUserDisplayNameUpdate(updatedUserRecord);
        // Return the new user object
        return updatedUserRecord;
    }

    public UserRecord addRole(UserRecord userRecord, RoleRecord roleRecord) {
        Identifier roleIdentifier = roleRecord.databaseIdentifier();
        for (Identifier _roleIdentifier : userRecord.roles()) {
            if (_roleIdentifier.equals(roleIdentifier)) {
                // If user already has role, just return it
                return userRecord;
            }
        }
        // Add role
        addArrayValue(userRecord.filter(),"roles", roleIdentifier);
        // Get new user
        UserRecord updatedUserRecord = get(userRecord.databaseIdentifier());
        // Execute update event
        Main.LISTENERS.onUserRoleUpdate(updatedUserRecord, roleRecord, true);
        // Reload value
        return updatedUserRecord;
    }

    public UserRecord removeRole(UserRecord userRecord, RoleRecord roleRecord) {
        // Add role
        pullArrayValue(userRecord.filter(),"roles", roleRecord.databaseIdentifier());
        // Get new user
        UserRecord updatedUserRecord = get(userRecord.databaseIdentifier());
        // Execute update event
        Main.LISTENERS.onUserRoleUpdate(updatedUserRecord, roleRecord, false);
        // Reload value
        return updatedUserRecord;
    }

    public UserRecord addConnection(UserRecord userRecord, Identifier connection) throws UserAlreadyRegisteredException, UserAlreadyConnectedException {
        if (get(connection) != null) {
            throw new UserAlreadyRegisteredException();
        }
        if (userRecord.connections().containsKey(connection.type())) {
            throw new UserAlreadyConnectedException();
        }
        // Add connection
        addArrayValue(userRecord.filter(), "connections", connection);
        // Get updated user
        UserRecord updatedUserRecord = get(userRecord.databaseIdentifier());
        // Update event
        Main.LISTENERS.onUserConnectionUpdate(updatedUserRecord, connection, true);
        // Return new user object
        return updatedUserRecord;
    }

    public UserRecord removeConnection(UserRecord userRecord, Identifier connection) {
        // Remove connection
        pullArrayValue(userRecord.filter(), "connections", connection);
        // Get updated user
        UserRecord updatedUserRecord = get(userRecord.databaseIdentifier());
        // Update event
        Main.LISTENERS.onUserConnectionUpdate(updatedUserRecord, connection, false);
        // Return new user object
        return updatedUserRecord;
    }

}
