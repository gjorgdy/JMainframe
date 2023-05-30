package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.InvalidDisplayNameException;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

public class DisplayNameHandler extends IdentifiersHandler {

    static final String DISPLAY_NAME = "display_name";

    public DisplayNameHandler(MongoCollection<Document> mongoCollection, boolean multiple, boolean duplicates) {
        super(mongoCollection, multiple, duplicates);
    }

    protected Document createDocument(String displayName) {
        return super.createDocument()
                .append(DISPLAY_NAME, format(displayName));
    }

    protected Document createDocument(Identifier identifier, String displayName) {
        return super.createDocument(identifier)
            .append(DISPLAY_NAME, format(displayName));
    }

    protected boolean setDisplayName(Bson filter, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        // Check if display name is already in use
        if (displayNameInUse(newDisplayName)) throw new InvalidDisplayNameException();
        // Set the value
        UpdateResult result = setValue(filter, DISPLAY_NAME, newDisplayName);
        // Execute update event if value changed
        return result.getModifiedCount() > 0;
    }

    public String getDisplayName(Bson filter) {
        return findOne(filter).getString(DISPLAY_NAME);
    }

    public Bson getFilter(String displayName) {
        String regexPattern = Pattern.quote(displayName);
        Pattern regexFilter = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Filters.regex(DISPLAY_NAME, regexFilter);
    }

    public boolean displayNameInUse(String displayName) {
        return exists(getFilter(displayName));
    }

    public static String format(String displayName) {
        return displayName.replace("_", " ");
    }

}
