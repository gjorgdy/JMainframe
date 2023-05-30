package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.handlers.generic.RolesHandler;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RoleHandler extends RolesHandler {

    // Keys
    //  extends RolesHandler -> DisplayNameHandler -> IdentifierHandler
    static final String PERMISSIONS = "permissions"; // List<String>
    static final String COLOR = "color"; // int

    public RoleHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection,true, true);
    }

    public boolean register(Identifier connection, @NotNull String displayName, int color) throws InvalidDisplayNameException, RecordAlreadyRegisteredException {
        if (exists(getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document roleDocument = createDocument(connection, displayName);
        roleDocument.append(PERMISSIONS, new ArrayList<String>());
        roleDocument.append(COLOR, color);
        // Insert into database
        InsertOneResult result = insert(roleDocument);
        // Return role
        return result.wasAcknowledged();
    }

    public boolean register(@NotNull String displayName, int color) throws InvalidDisplayNameException {
        // Throw an error if display name is already in use
        if (displayNameInUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document roleDocument = createDocument(displayName);
        roleDocument.append(PERMISSIONS, new String[0]);
        roleDocument.append(COLOR, color);
        // Insert into database
        InsertOneResult result = insert(roleDocument);
        // Return role
        return result.wasAcknowledged();
    }

    /**
     * Get ObjectIDs for all filtered roles and their parents
     *
     * @param roleFilters filters for the roles
     * @return list of all objectIDs
     */
    public List<ObjectId> getObjectIDs(List<Bson> roleFilters) {
        // Get document
        List<Bson> aggregateFilter = List.of(
                // Find main document
                Aggregates.match(Filters.or(roleFilters)),
                // Get all parent OIDs
                Aggregates.graphLookup("roles", "$roles", "_id", "_id", "parents"),
                // Add own OID to array
                Aggregates.project(Projections.fields(
                        Projections.computed("ids", new Document("$setUnion", List.of("$parents._id", List.of("$_id")))),
                        Projections.excludeId()
                )),
                // Unwind
                Aggregates.unwind("$ids"),
                // Group
                Aggregates.group(0, List.of(Accumulators.addToSet("ids", "$ids")))
        );
        Document document = aggregate(aggregateFilter);

        return document == null ? new ArrayList<>() : document.getList("ids", ObjectId.class);
    }

    public boolean setColor(Bson filter, int newColor) {
        UpdateResult result = setValue(filter, COLOR, 0xFFFFFF & newColor);
        return result.getModifiedCount() > 0;
    }

    public int getColor(Bson filter) throws NotRegisteredException {
        Document doc = findOne(filter);
        if (doc == null) throw new NotRegisteredException();
        return doc.containsKey(COLOR) ? doc.getInteger(COLOR) : 0xFFFFFF;
    }

    public boolean addPermission(Bson filter, String permission) throws NotRegisteredException {
        UpdateResult result = addArrayValue(filter, PERMISSIONS, permission);
        if (result.getModifiedCount() > 0) {
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onRolePermissionUpdate(roleIdentifiers, permission, true);
            return true;
        }
        return false;
    }

    public boolean removePermission(Bson filter, String permission) throws NotRegisteredException {
        UpdateResult result = pullArrayValue(filter, PERMISSIONS, permission);
        if (result.getModifiedCount() > 0) {
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onRolePermissionUpdate(roleIdentifiers, permission, false);
            return true;
        }
        return false;
    }

    public boolean addParentRole(Bson filter, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (addRole(filter, parentRoleIdentifier)) {
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            List<Identifier> parentRoleIdentifiers = getIdentifiers(getFilter(parentRoleIdentifier));
            // Execute update event
            Main.LISTENERS.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, true);
            // Reload value
            return true;
        }
        return false;
    }

    public boolean removeParentRole(Bson filter, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (removeRole(filter, parentRoleIdentifier)) {
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            List<Identifier> parentRoleIdentifiers = getIdentifiers(getFilter(parentRoleIdentifier));
            // Execute update event
            Main.LISTENERS.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, false);
            // Reload value
            return true;
        }
        return false;
    }

    public boolean addConnection(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException, RecordAlreadyConnectedException, RecordAlreadyRegisteredException {
        if (super.addIdentifier(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, true);
            return true;
        }
        return false;
    }

    public boolean removeConnection(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException {
        if (super.removeIdentifier(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> roleIdentifiers = getIdentifiers(filter);
            Main.LISTENERS.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, false);
            return true;
        }
        return false;
    }

}
