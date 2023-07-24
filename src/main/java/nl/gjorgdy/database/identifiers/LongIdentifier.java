package nl.gjorgdy.database.identifiers;

import java.util.Date;

public record LongIdentifier (
    Types type,
    Long id,
    Date linked
) implements Identifier {}
