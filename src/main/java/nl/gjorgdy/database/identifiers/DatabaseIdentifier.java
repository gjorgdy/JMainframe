package nl.gjorgdy.database.identifiers;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.Serializable;

public record DatabaseIdentifier (
        Types type,
        ObjectId id
) implements Identifier, Serializable {

    public Document filter() {
        return new Document("_id", id);
    }

}
