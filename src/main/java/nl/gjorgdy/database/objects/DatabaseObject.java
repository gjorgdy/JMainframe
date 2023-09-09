package nl.gjorgdy.database.objects;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import nl.gjorgdy.events.handlers.Events;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DatabaseObject {

    public final Events.Handler eventHandler;
    protected final ObjectId _id;
    private final MongoCollection<Document> mongoCollection;

    public DatabaseObject(MongoCollection<Document> mongoCollection, ObjectId _id, Events.Handler eventHandler) {
        this.mongoCollection = mongoCollection;
        this._id = _id;
        this.eventHandler = eventHandler;
    }

    public MongoCollection<Document> getMongoCollection() {
        return mongoCollection;
    }

    public Bson getFilter() {
        return new Document("_id", _id);
    }

    public Document getDocument() throws NotRegisteredException {
        Document doc = mongoCollection.find(getFilter()).first();
        if (doc == null) throw new NotRegisteredException();
        return doc;
    }

    public List<Identifier> getIdentifiers() throws NotRegisteredException {
        return new ArrayList<>();
    }

// Database handling

    /**
     * Insert a new document into a collection
     *
     * @param document the document to insert
     * @return the result of the insert
     */
    protected UpdateResult update(Document document) {
        return mongoCollection.updateOne(getFilter(), document);
    }

    /**
     * Remove a document from the database
     *
     * @return the result of the removal
     */
    protected DeleteResult remove() {
        return mongoCollection.deleteOne(getFilter());
    }

}
