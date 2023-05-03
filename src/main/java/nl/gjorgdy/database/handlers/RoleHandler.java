package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.UserAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.ConnectionsHandler;
import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;
import org.jetbrains.annotations.NotNull;

public class RoleHandler extends ConnectionsHandler<RoleRecord> {

    public RoleHandler(MongoCollection<RoleRecord> mongoCollection) {
        super(mongoCollection);
    }

    public RoleRecord create(@NotNull String displayName) throws InvalidDisplayNameException {
        // Check if display name is already in use
        if (isDisplayNameUsed(displayName)) {
            throw new InvalidDisplayNameException();
        }
        // Create a new user
        RoleRecord roleRecord = new RoleRecord(null, displayName, new String[0], new Identifier[0], new Identifier[0]);
        // Insert into database
        boolean success = insert(roleRecord).wasAcknowledged();
        // Return role
        return success ? roleRecord : null;
    }

    public RoleRecord addParentRole(RoleRecord roleRecord, RoleRecord parentRoleRecord) {
        // Add role
        addArrayValue(roleRecord.filter(), "parent_roles", parentRoleRecord.databaseIdentifier());
        // Get updated role
        RoleRecord updatedRoleRecord = get(roleRecord.databaseIdentifier());
        // Execute update event
        Main.LISTENERS.onRoleParentUpdate(updatedRoleRecord, parentRoleRecord, true);
        // Reload value
        return updatedRoleRecord;
    }

    public RoleRecord removeParentRole(RoleRecord roleRecord, RoleRecord parentRoleRecord) {
        // Add role
        addArrayValue(roleRecord.filter(), "parent_roles", parentRoleRecord.databaseIdentifier());
        // Get updated role
        RoleRecord updatedRoleRecord = get(roleRecord.databaseIdentifier());
        // Execute update event
        Main.LISTENERS.onRoleParentUpdate(updatedRoleRecord, parentRoleRecord, false);
        // Reload value
        return updatedRoleRecord;
    }

    @Override
    public RoleRecord addConnection(RoleRecord roleRecord, Identifier connection) throws UserAlreadyRegisteredException {
        RoleRecord updatedRoleRecord = super.addConnection(roleRecord, connection);
        // Execute update event
        Main.LISTENERS.onRoleConnectionUpdate(updatedRoleRecord, connection, true);
        // Return value
        return updatedRoleRecord;
    }

    @Override
    public RoleRecord removeConnection(RoleRecord roleRecord, Identifier connection) {
        RoleRecord updatedRoleRecord = super.removeConnection(roleRecord, connection);
        // Execute update event
        Main.LISTENERS.onRoleConnectionUpdate(updatedRoleRecord, connection, true);
        // Return value
        return updatedRoleRecord;
    }

}
