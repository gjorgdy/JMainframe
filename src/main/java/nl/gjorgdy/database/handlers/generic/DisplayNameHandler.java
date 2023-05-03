package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.records.RecordInterface;
import org.bson.Document;

public class DisplayNameHandler<T extends RecordInterface> extends CacheHandler<T> {

    public DisplayNameHandler(MongoCollection<T> mongoCollection) {
        super(mongoCollection);
    }

    public T setDisplayName(T record, String newDisplayName) throws InvalidDisplayNameException {
        // TODO : valid check
        // If display name is used
        if (isDisplayNameUsed(newDisplayName)) {
            throw new InvalidDisplayNameException();
        }
        // Update display name
        setValue(record.filter(), "display_name", newDisplayName);
        // Reload record
        return get(record.databaseIdentifier());
    }

    /**
     * Check if display name is used already
     *
     * @param displayName displayName to be checked
     * @return if the display is used already
     */
    protected boolean isDisplayNameUsed(String displayName) {
        return null != findOne(new Document("display_name", displayName));
    }


}
