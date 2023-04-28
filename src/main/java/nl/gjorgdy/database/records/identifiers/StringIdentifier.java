package nl.gjorgdy.database.records.identifiers;

import nl.gjorgdy.database.Records;

public record StringIdentifier (
        Types type,
        String id
) implements Identifier { }
