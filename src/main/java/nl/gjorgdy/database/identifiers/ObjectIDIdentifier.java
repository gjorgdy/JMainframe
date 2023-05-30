package nl.gjorgdy.database.identifiers;

import org.bson.types.ObjectId;

public record ObjectIDIdentifier(
        Types type,
        ObjectId id
) implements Identifier {}
