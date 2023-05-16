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

public class DisplayNameHandler extends IdentifiersHandler {

    static final String DISPLAY_NAME = "display_name";

    public DisplayNameHandler(MongoCollection<Document> mongoCollection, boolean multiple, boolean duplicates) {
        super(mongoCollection, multiple, duplicates);
    }

    protected Document createDocument(String displayName) {
        return super.create().append(DISPLAY_NAME, format(displayName));
    }

    protected boolean setDisplayName(Identifier identifier, String newDisplayName) throws InvalidDisplayNameException, NotRegisteredException {
        // Check if display name is already in use
        if (displayNameInUse(newDisplayName)) throw new InvalidDisplayNameException();
        // Set the value
        UpdateResult result = setValue(getFilter(identifier), DISPLAY_NAME, newDisplayName);
        // Execute update event if value changed
        return result.getModifiedCount() > 0;
    }

    public Document getFilter(String displayName) {
        TextSearchOptions tso = new TextSearchOptions()
                .caseSensitive(false)
                .diacriticSensitive(false);
        Bson textFilter = Filters.text(format(displayName), tso);
        return new Document(DISPLAY_NAME, textFilter);
    }

    public boolean displayNameInUse(String displayName) {
        return exists(getFilter(displayName));
    }

    public String format(String displayName) {
        return displayName.replace("_", " ");
    }

}
