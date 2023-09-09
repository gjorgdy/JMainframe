package nl.gjorgdy.database.objects.components;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.objects.DatabaseObject;
import nl.gjorgdy.events.Event;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class DatabaseComponent {

    protected final DatabaseObject parent;
    protected final String key;

    public DatabaseComponent(DatabaseObject parent, String key) {
        this.parent = parent;
        this.key = key;
    }

    protected void sendEvent(Event event) {
        parent.eventHandler.send(event);
    }

    public <T> T getEntry(Class<T> clazz) throws NotRegisteredException {
        return parent.getDocument().get(key, clazz);
    }

    public Document getEntry() throws NotRegisteredException {
        return parent.getDocument().get(key, Document.class);
    }

// Database handling

    /**
     * Update a single field value
     *
     * @param field the field to update
     * @param newValue the new value
     * @return the result of the update
     */
    protected UpdateResult setValue(String field, Object newValue) {
        return parent.getMongoCollection().updateOne(parent.getFilter(), Updates.set(field, newValue));
    }

    protected UpdateResult setValue(Object newValue) {
        return setValue(key, newValue);
    }
    /**
     * Add an object to an array
     *
     * @param arrayField the array to update
     * @param newValue the new object
     * @return the result of the update
     */
    protected UpdateResult addArrayValue(String arrayField, Object newValue) {
        return parent.getMongoCollection().updateOne(parent.getFilter(), Updates.addToSet(arrayField, newValue));
    }

    protected UpdateResult addArrayValue(Object newValue) {
        return addArrayValue(key, newValue);
    }

    protected UpdateResult addEachArrayValue(String arrayField, List<?> newValues) {
        return parent.getMongoCollection().updateOne(parent.getFilter(), Updates.addEachToSet(arrayField, newValues));
    }

    protected UpdateResult addEachArrayValue(List<?> newValues) {
        return addEachArrayValue(key, newValues);
    }

    /**
     * Remove an object from an array
     *
     * @param arrayField the array to update
     * @param oldValue the value to be removed
     * @return the result of the update
     */
    protected UpdateResult pullArrayValue(String arrayField, Object oldValue) {
        return parent.getMongoCollection().updateOne(parent.getFilter(), Updates.pull(arrayField, oldValue));
    }

    protected UpdateResult pullArrayValue(Object oldValue) {
        return pullArrayValue(key, oldValue);
    }

    protected UpdateResult pullAllArrayValue(String arrayField, List<?> oldValues) {
        return parent.getMongoCollection().updateOne(parent.getFilter(), Updates.pullAll(arrayField, oldValues));
    }

    protected UpdateResult pullAllArrayValue(List<?> oldValues) {
        return pullArrayValue(key, oldValues);
    }

    protected AggregateIterable<Document> aggregateIterable(List<Bson> aggregationFilter) {
        return parent.getMongoCollection().aggregate(aggregationFilter);
    }

    protected List<Document> aggregateList(List<Bson> aggregationFilter) {
        return parent.getMongoCollection().aggregate(aggregationFilter).into(new ArrayList<Document>());
    }

    protected Document aggregate(List<Bson> aggregationFilter) {
        return parent.getMongoCollection().aggregate(aggregationFilter).first();
    }

}
