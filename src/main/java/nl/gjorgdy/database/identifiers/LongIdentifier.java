package nl.gjorgdy.database.identifiers;

public record LongIdentifier (
    Types type,
    Long id
) implements Identifier {}
