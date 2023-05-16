package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class RoleHandler extends RolesHandler {

    // Keys
    //  extends DisplayNameHandler -> IdentifierHandler
    static final String PERMISSIONS = "permissions"; // String[]

    public RoleHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection,true, true);
    }

    public boolean register(@NotNull String displayName) throws InvalidDisplayNameException {
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document roleDocument = createDocument(displayName);
        roleDocument.append(PERMISSIONS, new String[0]);
        // Insert into database
        InsertOneResult result = insert(roleDocument);
        // Return role
        return result.wasAcknowledged();
    }

    public boolean addPermission(Identifier roleIdentifier, String permission) throws NotRegisteredException {
        UpdateResult result = addArrayValue(getFilter(roleIdentifier), PERMISSIONS, permission);
        if (result.getModifiedCount() > 0) {
            Identifier[] roleIdentifiers = getAllIdentifiers(getFilter(roleIdentifier));
            Main.LISTENERS.onRolePermissionUpdate(roleIdentifiers, permission, true);
            return true;
        }
        return false;
    }

    public boolean removePermission(Identifier roleIdentifier, String permission) throws NotRegisteredException {
        UpdateResult result = pullArrayValue(getFilter(roleIdentifier), PERMISSIONS, permission);
        if (result.getModifiedCount() > 0) {
            Identifier[] roleIdentifiers = getAllIdentifiers(getFilter(roleIdentifier));
            Main.LISTENERS.onRolePermissionUpdate(roleIdentifiers, permission, false);
            return true;
        }
        return false;
    }

    public boolean addParentRole(Identifier roleIdentifier, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (addRole(roleIdentifier, parentRoleIdentifier)) {
            Identifier[] roleIdentifiers = getAllIdentifiers(getFilter(roleIdentifier));
            Identifier[] parentRoleIdentifiers = getAllIdentifiers(getFilter(parentRoleIdentifier));
            // Execute update event
            Main.LISTENERS.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, true);
            // Reload value
            return true;
        }
        return false;
    }

    public boolean removeParentRole(Identifier roleIdentifier, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (removeRole(roleIdentifier, parentRoleIdentifier)) {
            Identifier[] roleIdentifiers = getAllIdentifiers(getFilter(roleIdentifier));
            Identifier[] parentRoleIdentifiers = getAllIdentifiers(getFilter(parentRoleIdentifier));
            // Execute update event
            Main.LISTENERS.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, false);
            // Reload value
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

    public boolean removeConnection(Identifier roleIdentifier, Identifier connectionIdentifier) throws NotRegisteredException {
        if (super.removeIdentifier(roleIdentifier, connectionIdentifier)) {
            // Execute update event
            Identifier[] roleIdentifiers = getAllIdentifiers(roleIdentifier);
            Main.LISTENERS.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, false);
            return true;
        }
        return false;
    }

}
