package nl.gjorgdy.database.identifiers;

import java.io.Serializable;

public record LongIdentifier (
        Types type,
        Long id
) implements Identifier, Serializable { }
