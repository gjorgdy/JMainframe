package nl.gjorgdy.database.handlers.components;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

public class SyncRulesHandler extends DatabaseHandler {

    static final String SYNC = "sync";
    static final String SYNC_DISPLAY_NAMES = "display_names";
    static final String SYNC_ROLES = "roles";

    public SyncRulesHandler(MongoCollection<Document> mongoCollection) {
        super(mongoCollection);
    }

    public void writeDocument(Document document, boolean displayNames, boolean roles) {
        document.append(SYNC,
                new Document(SYNC_DISPLAY_NAMES, displayNames)
                        .append(SYNC_ROLES, roles)
        );
    }

    public boolean doesSyncDisplayNames(Bson filter) {
        Document serverDocument = findOne(filter);
        return serverDocument != null && serverDocument.get(SYNC, Document.class).getBoolean(SYNC_DISPLAY_NAMES);
    }

    public boolean doesSyncRoles(Bson filter) {
        Document serverDocument = findOne(filter);
        return serverDocument == null || serverDocument.get(SYNC, Document.class).getBoolean(SYNC_ROLES);
    }

    public boolean setSyncDisplayNames(Bson filter, boolean value) {
        return setValue(filter, (SYNC + "." + SYNC_DISPLAY_NAMES), value).getModifiedCount() > 0;
    }

    public boolean setSyncRoles(Bson filter, boolean value) {
        return setValue(filter, (SYNC + "." + SYNC_ROLES), value).getModifiedCount() > 0;
    }

}
