package nl.gjorgdy.database.identifiers;

public record StringIdentifier(
        Types type,
        String id
) implements Identifier {}
