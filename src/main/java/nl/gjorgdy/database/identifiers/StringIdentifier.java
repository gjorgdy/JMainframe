package nl.gjorgdy.database.identifiers;

import java.io.Serializable;

public record StringIdentifier (
        Types type,
        String id
) implements Identifier, Serializable {}
