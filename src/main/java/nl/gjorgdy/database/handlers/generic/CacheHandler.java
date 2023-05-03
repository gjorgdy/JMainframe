package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.records.RecordInterface;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CacheHandler<T extends RecordInterface> extends DatabaseHandler<T> {

    protected final Map<Identifier, ObjectIdIdentifier> identifierCache = new HashMap<>();
    protected final Map<ObjectIdIdentifier, T> recordCache = new HashMap<>();
    public CacheHandler(MongoCollection<T> mongoCollection) {
        super(mongoCollection);
        load();
    }

    @Nullable
    public Collection<T> getAll() {
        return recordCache.values();
    }

    @Nullable
    public T get(Identifier identifier) {
        if (identifier instanceof ObjectIdIdentifier) {
            return recordCache.get(identifier);
        } else {
            return recordCache.get(identifierCache.get(identifier));
        }
    }

    /**
     * Remove record from cache and database
     *
     * @param record to remove
     */
    public void remove(T record) {
        // Remove from identifier cache
        for (Map.Entry<Identifier, ObjectIdIdentifier> entry : identifierCache.entrySet()) {
            if (entry.getValue() == record.databaseIdentifier()) {
                identifierCache.remove(entry.getKey());
            }
        }
        // Remove from record cache
        recordCache.remove(record.databaseIdentifier());
        // Remove from database
        remove(record.filter());
        // Run garbage collector
        System.gc();
    }

    /**
     * Load all records from the database into the cache
     *
     */
    public void load() {
        // get all records
        FindIterable<T> findIterable = find(new Document());
        // store all records into cache
        findIterable.forEach(iterable -> {
            store(iterable, true);
        });
    }

    /**
     * Reload specified record
     *
     */
    private T reload(BsonValue objectId, boolean storeIdentifiers) {
        // get all records
        T newRecord = findOne(new Document("_id", objectId));
        // Store record in map
        if (newRecord != null) store(newRecord, storeIdentifiers);
        // Return record
        return newRecord;
    }

    /**
     * Reload an existing record into cache
     *
     * @param record to reload
     * @return the newly loaded record
     */
    private T reload(@NotNull T record, boolean storeIdentifiers) {
        T newRecord = findOne(record.filter());
        // Store record in map
        if (newRecord != null) store(newRecord, storeIdentifiers);
        // Return record
        return newRecord;
    }

    /**
     * Store record in cache
     *
     * @param record to store
     */
    private void store(@NotNull T record, boolean storeIdentifiers) {
        ObjectIdIdentifier databaseIdentifier = record.databaseIdentifier();
        // Put record into recordCache (auto overwrites)
        recordCache.put(databaseIdentifier, record);
        // Remove identifiers from cache if enabled
        if (storeIdentifiers) {
            List<Identifier> toRemove = new ArrayList<>();
            identifierCache.forEach((identifier, _databaseIdentifier) -> {
                // If record in cache equals id of updated record
                if (_databaseIdentifier.equals(databaseIdentifier)) {
                    // Remove record from cache
                    toRemove.add(identifier);
                }
            });
            toRemove.forEach(identifierCache::remove);
            // Add identifiers to cache
            for (Identifier identifier : record.identifiers()) {
                identifierCache.put(identifier, databaseIdentifier);
            }
        }
        // Run garbage collector to remove old instance from cache
        System.gc();
    }

// Load when necessary
    @Override
    protected InsertOneResult insert(T document) {
        InsertOneResult insertOneResult = super.insert(document);
        store(document, true);
        return insertOneResult;
    }

    @Override
    protected UpdateResult addArrayValue(Bson filter, String arrayField, Object newValue) {
        UpdateResult updateResult = super.addArrayValue(filter, arrayField, newValue);
        reload(updateResult.getUpsertedId(), true);
        return updateResult;
    }

    @Override
    protected UpdateResult pullArrayValue(Bson filter, String arrayField, Object oldValue) {
        UpdateResult updateResult = super.pullArrayValue(filter, arrayField, oldValue);
        reload(updateResult.getUpsertedId(), true);
        return updateResult;
    }

    @Override
    protected UpdateResult setValue(Bson filter, String field, Object newValue) {
        UpdateResult updateResult = super.setValue(filter, field, newValue);
        reload(updateResult.getUpsertedId(), true);
        return updateResult;
    }
}