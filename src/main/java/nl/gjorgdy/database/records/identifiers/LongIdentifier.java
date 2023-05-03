package nl.gjorgdy.database.records.identifiers;

import org.bson.Document;

import java.io.Serializable;

public record LongIdentifier (
        Types type,
        Long id
) implements Identifier, Serializable {

    public Document filter(String array) {
        return new Document(array + "." + type.toString(), id);
    }

}
