package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.exceptions.RecordAlreadyConnectedException;
import nl.gjorgdy.database.exceptions.RecordAlreadyRegisteredException;
import nl.gjorgdy.database.identifiers.DatabaseIdentifier;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

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

    protected Document create() {
        return new Document(KEY, new Identifier[0]);
    }

    protected boolean addIdentifier(Identifier userIdentifier, Identifier connection) throws RecordAlreadyRegisteredException, RecordAlreadyConnectedException {
        // Throw error if connection is already used
        if (exists(getFilter(connection))) throw new RecordAlreadyRegisteredException();
        // If multiple is disabled, check if array already has a value
        if (!MULTIPLE && findOne(getFilter(userIdentifier)).getList(KEY, Identifier.class).size() > 1) throw new RecordAlreadyConnectedException();
        // Throw error if an identifier of this type is already registered and duplicates is disabled
        if (!DUPLICATES && exists(
                getFilter(userIdentifier)
                .append(
                        KEY,
                    Filters.elemMatch(
                            connection.type().toString(),
                            new Document("$exists", true)
                    )
                )
            )
        ) throw new RecordAlreadyConnectedException();
        // Add connection
        UpdateResult updateResult = addArrayValue(getFilter(userIdentifier), KEY, connection);
        // Reload record
        return updateResult.getModifiedCount() > 0;
    }

    protected boolean removeIdentifier(Identifier userIdentifier, Identifier connection) {
        // Add connection
        UpdateResult updateResult = pullArrayValue(getFilter(userIdentifier), KEY, connection);
        // Reload record
        return updateResult.getModifiedCount() > 0;
    }

    public ObjectId getObjectID(Identifier roleIdentifier) throws NotRegisteredException {
        return super.getObjectID(getFilter(roleIdentifier));
    }

    public Document getFilter(Identifier.Types type, Object id) {
        return new Document(KEY + "." + type, id);
    }

    public Document getFilter(Identifier identifier) {
        // If database identifier, return object id filter
        if (identifier instanceof DatabaseIdentifier) return new Document("_id", identifier.id());
        // Otherwise just return constructed filter
        return new Document(KEY + "." + identifier.type(), identifier.id());
    }

    public Identifier[] getAllIdentifiers(Identifier identifier) throws NotRegisteredException {
        return getAllIdentifiers(getFilter(identifier));
    }

    public Identifier[] getAllIdentifiers(Document filter) throws NotRegisteredException {
        Document document = findOne(filter);
        if (document == null) throw new NotRegisteredException();
        List<Identifier> list = document.getList(KEY, Identifier.class);
        return list.toArray(new Identifier[0]);
    }

}
