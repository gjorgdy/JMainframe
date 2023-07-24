package nl.gjorgdy.database.handlers.components;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.Mainframe;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.Types;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RolesHandler extends DatabaseHandler {

    // Key
    static final String KEY = "roles"; // List<ObjectID>
    private final IdentifiersHandler identifiersHandler;

    public RolesHandler(MongoCollection<Document> mongoCollection, IdentifiersHandler identifiersHandler) {
        super(mongoCollection);
        this.identifiersHandler = identifiersHandler;
    }

    public void writeDocument(@NotNull Document document) {
        document.append(KEY, new ArrayList<ObjectId>());
    }

    /**
     * Get the identifiers of the roles assigned to filtered document
     *
     * @param filter filter of the document
     * @return list of all identifiers assigned to document
     * @throws NotRegisteredException if filter does not match document
     */
    public List<Identifier> getIdentifiers(Bson filter) throws NotRegisteredException {
        // Get document
        List<Bson> aggregateFilter = List.of(
                // Find main document
                Aggregates.match(filter),
                // Get all associated roles
                Aggregates.lookup("roles", KEY, "_id", KEY),
                // Project the documents into a roles field
                Aggregates.project(Projections.computed("idt", "$roles.identifiers")),
                // Take values out of arrays
                Aggregates.unwind("$idt"),
                Aggregates.unwind("$idt"),
                // Project the identifier onto the document root
                Aggregates.project(Projections.fields(
                        Projections.computed("type", "$idt.type"),
                        Projections.computed("id", "$idt.id")
                )),
                // Remove ID from resulting document
                Aggregates.project(Projections.excludeId())
        );
        List<Document> identifierDocuments = aggregateList(aggregateFilter);
        // If null, throw exception
        if (identifierDocuments == null)
            throw new NotRegisteredException();
        // Decode list into identifiers and return
        return identifierDocuments.stream().map(doc ->
                Identifier.create(Types.valueOf(doc.getString("type")), doc.get("id"))
        ).toList();
    }

    public List<ObjectId> getDocumentsRoleObjectIDs(Bson filter) throws NotRegisteredException {
        // Get document
        List<Bson> aggregateFilter = List.of(
                // Find main document
                Aggregates.match(filter),
                // Get all parent OIDs
                Aggregates.graphLookup("roles", "$roles", "roles", "_id", "parents"),
                // Add own OID to array
                Aggregates.project(Projections.fields(
                        Projections.computed("ids", new Document("$setUnion", List.of("$parents._id", "$roles"))),
                        Projections.excludeId()
                ))
        );
        Document objectIDsDocument = aggregate(aggregateFilter);
        // If null, throw exception
        if (objectIDsDocument == null) throw new NotRegisteredException();
        // Decode list into identifiers and return
        return objectIDsDocument.getList("ids", ObjectId.class);
    }

    public boolean add(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        return add(filter, List.of(roleIdentifier));
    }

    public boolean add(Bson documentFilter, List<Identifier> roleIdentifiers) throws NotRegisteredException {
        if (roleIdentifiers.size() == 0) return false;
        // Get role objectIDs
        List<Bson> roleFilters = identifiersHandler.getFilters(roleIdentifiers);
        List<ObjectId> roleObjectIds = Mainframe.ROLES.getObjectIDs(roleFilters);
        // Add role DB ID to user
        UpdateResult updateResult = addEachArrayValue(documentFilter, KEY, roleObjectIds);
        // Return if a value was changed
        return updateResult.getModifiedCount() > 0;
    }

    public boolean remove(Bson filter, Identifier roleIdentifier) throws NotRegisteredException {
        return remove(filter, List.of(roleIdentifier));
    }

    public boolean remove(Bson documentFilter, List<Identifier> roleIdentifiers) throws NotRegisteredException {
        // Get role identifiers
        List<Bson> roleFilters = identifiersHandler.getFilters(roleIdentifiers);
        // Remove roles from document
        List<ObjectId> roleObjectIds = Mainframe.ROLES.getObjectIDs(roleFilters);
        UpdateResult removeUpdateResult = pullAllArrayValue(documentFilter, KEY, roleObjectIds);
        // Re-add parent roles if necessary
        try {
            List<ObjectId> updatedRoleObjectIds = getDocumentsRoleObjectIDs(documentFilter);
            UpdateResult addUpdateResult = addEachArrayValue(documentFilter, KEY, updatedRoleObjectIds);
        } catch (NotRegisteredException e) {
            System.err.println(e.toString());
        }
        // Return true if a value was changed
        return removeUpdateResult.getModifiedCount() > 0;
    }

}
