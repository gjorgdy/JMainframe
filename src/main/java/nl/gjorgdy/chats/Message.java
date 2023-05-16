package nl.gjorgdy.chats;

import nl.gjorgdy.database.identifiers.Identifier;

import java.io.Serializable;

public record Message (
        Identifier author_identifier,
        Identifier source_identifier,
        String content
) implements Serializable {}
