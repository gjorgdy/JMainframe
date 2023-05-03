package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;

public class DatabaseHandler<T> {

    private final MongoCollection<T> mongoCollection;
    public DatabaseHandler(MongoCollection<T> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

// Database handling
    public void clear() {
        mongoCollection.deleteMany(new BsonDocument());
    }
    @Nullable
    protected T findOne(Bson filter) {
        return find(filter).first();
    }

    protected FindIterable<T> find(Bson filter) {
        return mongoCollection.find(filter);
    }
    protected InsertOneResult insert(T document) {
        return mongoCollection.insertOne(document);
    }
    protected DeleteResult remove(Bson filter) {
        return mongoCollection.deleteOne(filter);
    }
    protected UpdateResult setValue(Bson filter, String field, Object newValue) {
        return mongoCollection.updateOne(filter, Updates.set(field, newValue));
    }
    protected UpdateResult addArrayValue(Bson filter, String arrayField, Object newValue) {
        return mongoCollection.updateOne(filter, Updates.push(arrayField, newValue));
    }
    protected UpdateResult pullArrayValue(Bson filter, String arrayField, Object oldValue) {
        return mongoCollection.updateOne(filter, Updates.pull(arrayField, oldValue));
    }

}
