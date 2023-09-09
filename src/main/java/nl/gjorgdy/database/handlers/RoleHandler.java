package nl.gjorgdy.database.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
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
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RoleHandler extends DatabaseHandler {

    // Components
    static final String PERMISSIONS = "permissions"; // List<String>
    static final String COLOR = "color"; // int
    private final DisplayNameHandler DISPLAY_NAME;
    private final IdentifiersHandler IDENTIFIERS;
    private final RolesHandler PARENT_ROLES;

    public RoleHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
        IDENTIFIERS = new IdentifiersHandler(mongoCollection, true, true);
        DISPLAY_NAME = new DisplayNameHandler(mongoCollection);
        PARENT_ROLES = new RolesHandler(mongoCollection, IDENTIFIERS);
    }

    public boolean register(@Nullable Identifier connection, @NotNull String displayName, int color) throws InvalidDisplayNameException, RecordAlreadyRegisteredException {
        if (connection != null && exists(IDENTIFIERS.getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // Throw an error if display name is already in use
        if (DISPLAY_NAME.inUse(displayName)) throw new InvalidDisplayNameException();
        // Create a new user
        Document roleDocument = new Document();
        DISPLAY_NAME.writeDocument(roleDocument, displayName);
        if (connection != null)
            IDENTIFIERS.writeDocument(roleDocument, connection);
        else
            IDENTIFIERS.writeDocument(roleDocument);
        PARENT_ROLES.writeDocument(roleDocument);
        roleDocument.append(PERMISSIONS, new ArrayList<String>());
        roleDocument.append(COLOR, color);
        // Insert into database
        InsertOneResult result = insert(roleDocument);
        // Return role
        return result.wasAcknowledged();
    }

    public boolean register(@NotNull String displayName, int color) throws InvalidDisplayNameException {
        try {
            return register(null, displayName, color);
        } catch (RecordAlreadyRegisteredException e) {
            return false;
        }
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
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            Mainframe.OldEvents.onRolePermissionUpdate(roleIdentifiers, permission, true);
            return true;
        }
        return false;
    }

    public boolean removePermission(Bson filter, String permission) throws NotRegisteredException {
        UpdateResult result = pullArrayValue(filter, PERMISSIONS, permission);
        if (result.getModifiedCount() > 0) {
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            Mainframe.OldEvents.onRolePermissionUpdate(roleIdentifiers, permission, false);
            return true;
        }
        return false;
    }

    public boolean addParentRole(Bson filter, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (PARENT_ROLES.add(filter, parentRoleIdentifier)) {
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            List<Identifier> parentRoleIdentifiers = IDENTIFIERS.getAll(IDENTIFIERS.getFilter(parentRoleIdentifier));
            // Execute update event
            Mainframe.OldEvents.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, true);
            // Reload value
            return true;
        }
        return false;
    }

    public boolean removeParentRole(Bson filter, Identifier parentRoleIdentifier) throws NotRegisteredException {
        if (PARENT_ROLES.remove(filter, parentRoleIdentifier)) {
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            List<Identifier> parentRoleIdentifiers = IDENTIFIERS.getAll(IDENTIFIERS.getFilter(parentRoleIdentifier));
            // Execute update event
            Mainframe.OldEvents.onRoleParentUpdate(roleIdentifiers, parentRoleIdentifiers, false);
            // Reload value
            return true;
        }
        return false;
    }

    public boolean addLink(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException, RecordAlreadyConnectedException, RecordAlreadyRegisteredException {
        if (IDENTIFIERS.add(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            Mainframe.OldEvents.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, true);
            return true;
        }
        return false;
    }

    public boolean removeLink(Bson filter, Identifier connectionIdentifier) throws NotRegisteredException {
        if (IDENTIFIERS.remove(filter, connectionIdentifier)) {
            // Execute update event
            List<Identifier> roleIdentifiers = IDENTIFIERS.getAll(filter);
            Mainframe.OldEvents.onUserConnectionUpdate(roleIdentifiers, connectionIdentifier, false);
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
