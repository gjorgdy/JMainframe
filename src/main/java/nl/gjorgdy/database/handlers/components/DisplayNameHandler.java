package nl.gjorgdy.database.handlers.components;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class DisplayNameHandler extends DatabaseHandler {

    public final String KEY = "display_name";

    public DisplayNameHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
    }

    public void writeDocument(Document document, String displayName) throws InvalidDisplayNameException {
        if (inUse(displayName)) throw new InvalidDisplayNameException();
        document.append(KEY, displayName);
    }

    public boolean set(Bson filter, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        // Check if display name is already in use
        if (inUse(filter, newDisplayName)) throw new InvalidDisplayNameException();
        // Set the value
        UpdateResult result = setValue(filter, KEY, newDisplayName);
        // Execute update event if value changed
        return result.getModifiedCount() > 0;
    }

    public String get(Bson filter) throws NotRegisteredException {
        Document doc = findOne(filter);
        if (doc == null) throw new NotRegisteredException();
        return doc.getString(KEY);
    }

    public Bson getFilter(String displayName) {
        String regexPattern = "^" + displayName + "$";
        Pattern regexFilter = Pattern.compile(regexPattern,Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Filters.regex(KEY, regexFilter);
    }

    public boolean inUse(Bson filter, String newDisplayName) {
        return exists(Filters.and(
                getFilter(newDisplayName),
                Filters.not(filter)
        ));
    }

    public boolean inUse(String displayName) {
        return exists(getFilter(displayName));
    }

    public static String format(String displayName) {
        return displayName.replace("_", " ");
    }

}
