package nl.gjorgdy.database.records.identifiers;

import nl.gjorgdy.database.Records;

public interface Identifier {

    enum Types {
        discord_user,
        discord_role,
        minecraft_user
    }

    Types type();
    Object id();
}
