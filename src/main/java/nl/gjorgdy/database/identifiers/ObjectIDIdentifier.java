package nl.gjorgdy.database.identifiers;

import org.bson.types.ObjectId;

import java.util.Date;

public record ObjectIDIdentifier(
    Types type,
    ObjectId id,
    Date linked
) implements Identifier {}
