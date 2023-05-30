package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.database.identifiers.ObjectIDIdentifier;
import nl.gjorgdy.database.identifiers.Types;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdentifiersHandler extends DatabaseHandler {

    // Keys
    public static final String KEY = "identifiers"; // Identifier
    final boolean MULTIPLE;
    final boolean DUPLICATES;

    protected IdentifiersHandler(MongoCollection<Document> mongoCollection, boolean multiple, boolean duplicates) {
        super(mongoCollection);
        MULTIPLE = multiple;
        DUPLICATES = duplicates;
    }

    protected Document createDocument() {
        return new Document(KEY, new ArrayList<Identifier>());
    }

    protected Document createDocument(Identifier identifier) {
        return new Document(KEY, List.of(identifier));
    }

    protected boolean addIdentifier(Bson filter, Identifier connection) throws RecordAlreadyRegisteredException, RecordAlreadyConnectedException {
        // Throw error if connection is already used
        if (exists(getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // If multiple is disabled, check if array already has a value
        if (!MULTIPLE && findOne(filter).getList(KEY, Identifier.class).size() > 1) throw new RecordAlreadyConnectedException();
        // Throw error if an identifier of this type is already registered and duplicates is disabled
        if (!DUPLICATES && exists(Filters.and(
                filter,
                Filters.elemMatch(
                    KEY,
                    new Document("type", connection.type().toString())
                    )
                )
            )
        ) throw new RecordAlreadyConnectedException();
        // Add connection
        UpdateResult updateResult = addArrayValue(filter, KEY, connection);
        // Reload record
        return updateResult.getModifiedCount() > 0;
    }

    protected boolean removeIdentifier(Bson filter, Identifier connection) {
        // Add connection
        UpdateResult updateResult = pullArrayValue(filter, KEY, connection);
        // Reload record
        return updateResult.getModifiedCount() > 0;
    }

    public Bson getFilter(ObjectId objectId) {
        return new Document("_id", objectId);
    }

    public List<Bson> getFilters(List<Identifier> identifiers) {
        return identifiers.parallelStream().map(idt ->
            getFilter(idt.type(), idt.id())
        ).toList();
    }

    public Bson getFilter(Identifier identifier) {
        // If database identifier, return object id filter
        if (identifier instanceof ObjectIDIdentifier) return new Document("_id", identifier.id());
        // Otherwise just return constructed filter
        return getFilter(identifier.type(), identifier.id());
    }

    public Bson getFilter(Types type, Object id) {
        return Filters.elemMatch(
                KEY,
                new Document("type",type.toString())
                        .append("id", id)
        );
    }

    public List<Identifier> getIdentifiers(Bson filter) {
        return getIdentifiers(List.of(filter));
    }

    public List<Identifier> getIdentifiers(List<Bson> filters) {
        // Get document
        List<Bson> aggregateFilter = List.of(
                // Find document
                Aggregates.match(Filters.or(filters)),
                // Unwind
                Aggregates.unwind("$identifiers"),
                // Project
                Aggregates.project(Projections.fields(
                    Projections.include("identifiers"),
                    Projections.excludeId()
                ))
        );
        List<Document> documents = aggregateList(aggregateFilter);

        return documents.parallelStream().map(
                doc -> {
                    Document identifierDocument = doc.get("identifiers", Document.class);
                    String type = identifierDocument.getString("type");
                    Object   id = identifierDocument.get("id");
                    if (type != null)
                        return Identifier.create(Types.valueOf(type), id);
                    else return null;
                }
        ).filter(Objects::nonNull).toList();
    }

}
