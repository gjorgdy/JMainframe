package nl.gjorgdy.database.records;

import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.ObjectIdIdentifier;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public interface RecordInterface {

    List<String> toStringList();
    default String toFormattedString() {
        return String.join("\n", toStringList());
    }

    default Document filter() {
        return new Document("_id", _id());
    }
    ObjectId _id();
    Identifier[] identifiers();
    ObjectIdIdentifier databaseIdentifier();

}
