package nl.gjorgdy.database.records.identifiers;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.Serializable;

public record ObjectIdIdentifier (
        Types type,
        ObjectId id
) implements Identifier, Serializable {

    public Document filter() {
        return new Document("_id", id);
    }

}
