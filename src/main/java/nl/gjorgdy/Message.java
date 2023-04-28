package nl.gjorgdy;

import nl.gjorgdy.database.records.identifiers.Identifier;

public record Message (
        Identifier authorIdentifier,
        String content
) {}
