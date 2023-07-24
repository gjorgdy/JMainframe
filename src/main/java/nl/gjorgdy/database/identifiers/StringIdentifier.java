package nl.gjorgdy.database.identifiers;

import java.util.Date;

public record StringIdentifier(
    Types type,
    String id,
    Date linked
) implements Identifier {}
