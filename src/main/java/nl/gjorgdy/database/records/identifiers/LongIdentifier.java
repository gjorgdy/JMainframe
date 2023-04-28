package nl.gjorgdy.database.records.identifiers;

import nl.gjorgdy.database.Records;

public record LongIdentifier (
        Types type,
        Long id
) implements Identifier { }
