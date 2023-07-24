package nl.gjorgdy.database.handlers.components;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import org.bson.Document;
import org.bson.conversions.Bson;

public class AffixHandler extends DatabaseHandler {

    public final String KEY = "affix";
    public final String PREFIX_KEY = "prefix";
    public final String SUFFIX_KEY = "suffix";

    public void writeDocument(Document document, String prefix, String suffix) {
        document.append(KEY,
            new Document(PREFIX_KEY, prefix)
                    .append(SUFFIX_KEY, suffix)
        );
    }

    public AffixHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
    }

    public boolean setPrefix(Bson filter, String prefix) {
        // Set the value
        UpdateResult result = setValue(filter, KEY + "." + PREFIX_KEY, prefix);
        // Execute update event if value changed
        return result.getModifiedCount() > 0;
    }

    public boolean setSuffix(Bson filter, String suffix) {
        // Set the value
        UpdateResult result = setValue(filter, KEY + "." + SUFFIX_KEY, suffix);
        // Execute update event if value changed
        return result.getModifiedCount() > 0;
    }

    public String[] get(Bson filter) throws NotRegisteredException {
        Document doc = findOne(filter);
        if (doc == null) throw new NotRegisteredException();
        return new String[]{
            doc.get(KEY, Document.class).getString(PREFIX_KEY),
            doc.get(KEY, Document.class).getString(SUFFIX_KEY)
        };
    }

}
