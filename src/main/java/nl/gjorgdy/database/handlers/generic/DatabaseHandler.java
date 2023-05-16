package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

// Instance creation
    private final MongoCollection<Document> mongoCollection;

    public DatabaseHandler(MongoCollection<Document> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

// Database handling

    /**
     * Insert a new document into a collection
     *
     * @param document the document to insert
     * @return the result of the insert
     */
    protected InsertOneResult insert(Document document) {
    return mongoCollection.insertOne(document);
    }

    /**
     * Remove a document from the database
     *
     * @param filter the filter for the document
     * @return the result of the removal
     */
    protected DeleteResult remove(Bson filter) {
        return mongoCollection.deleteOne(filter);
    }

    /**
     * Clear a whole collection
     * !! USE WITH CARE !!
     *
     * @return the result of the deletion
     */
    public DeleteResult clear() {
        return mongoCollection.deleteMany(new BsonDocument());
    }

    /**
     * Get the ObjectID (Database ID) of a document
     *
     * @param filter the filter for the document
     * @return the ObjectID of the document
     * @throws NotRegisteredException if no document was found
     */
    public ObjectId getObjectID(Bson filter) throws NotRegisteredException {
        // Get document for role
        Document document = findOne(filter);
        // If role is not registered, throw exception
        if (document == null) throw new NotRegisteredException();
        // Get the ID of the role
        return document.getObjectId("_id");
    }

    protected boolean exists(Bson filter) { return mongoCollection.countDocuments(filter) > 0; }

    @Nullable
    protected Document findOne(Bson filter) {
        return mongoCollection.find(filter).first();
    }

    protected List<Document> find(Bson filter) {
        return mongoCollection.find(filter).into(new ArrayList<Document>());
    }

    /**
     * Update a single field value
     *
     * @param filter the filter for the document
     * @param field the field to update
     * @param newValue the new value
     * @return the result of the update
     */
    protected UpdateResult setValue(Bson filter, String field, Object newValue) {
        return mongoCollection.updateOne(filter, Updates.set(field, newValue));
    }

    /**
     * Add an object to an array
     *
     * @param filter the filter for the document
     * @param arrayField the array to update
     * @param newValue the new object
     * @return the result of the update
     */
    protected UpdateResult addArrayValue(Bson filter, String arrayField, Object newValue) {
        return mongoCollection.updateOne(filter, Updates.addToSet(arrayField, newValue));
    }

    /**
     * Remove an object from an array
     *
     * @param filter the filter for the document
     * @param arrayField the array to update
     * @param oldValue the value to be removed
     * @return the result of the update
     */
    protected UpdateResult pullArrayValue(Bson filter, String arrayField, Object oldValue) {
        return mongoCollection.updateOne(filter, Updates.pull(arrayField, oldValue));
    }

}
