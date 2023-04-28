package nl.gjorgdy.database.records.identifiers;

import nl.gjorgdy.database.Records;
import org.bson.types.ObjectId;

public record ObjectIdIdentifier (
        Types type,
        ObjectId id
) implements Identifier { }
