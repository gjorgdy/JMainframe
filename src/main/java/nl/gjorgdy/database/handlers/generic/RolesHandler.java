package nl.gjorgdy.database.handlers.generic;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import nl.gjorgdy.Main;
import nl.gjorgdy.database.exceptions.NotRegisteredException;
import nl.gjorgdy.database.identifiers.Identifier;
import org.bson.Document;
import org.bson.types.ObjectId;

public class RolesHandler extends DisplayNameHandler {

    // Key
    static final String ROLES = "roles"; // DatabaseIdentifier

    @Override
    protected Document createDocument(String displayName) {
        return super.createDocument(displayName)
                .append(ROLES, new ObjectId[0]);
    }

    protected RolesHandler(MongoCollection<Document> mongoCollection, boolean multiple, boolean duplicates) {
        super(mongoCollection, multiple, duplicates);
    }

    protected boolean addRole(Identifier userIdentifier, Identifier roleIdentifier) throws NotRegisteredException {
        // Get role identifiers
        ObjectId roleObjectId = Main.MONGODB.roleHandler.getObjectID(roleIdentifier);
        // Add role DB ID to user
        UpdateResult updateResult = addArrayValue(getFilter(userIdentifier), ROLES, roleObjectId);
        // Return if a value was changed
        return updateResult.getModifiedCount() > 0;
    }

    protected boolean removeRole(Identifier userIdentifier, Identifier roleIdentifier) throws NotRegisteredException {
        // Get role identifier
        ObjectId roleObjectId = Main.MONGODB.roleHandler.getObjectID(roleIdentifier);
        // Remove role DB ID from user
        UpdateResult updateResult = pullArrayValue(getFilter(userIdentifier), ROLES, roleObjectId);
        // Return if a value was changed
        return updateResult.getModifiedCount() > 0;
    }

}
