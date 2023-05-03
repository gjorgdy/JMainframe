package nl.gjorgdy.database.records.identifiers;

import org.bson.Document;

import java.io.Serializable;

public interface Identifier {

    enum Types {
        mainframe_user,
        mainframe_role,
        mainframe_channel,
        mainframe_server,
        discord_user,
        discord_role,
        discord_guild,
        discord_channel,
        minecraft_user
    }

    Types type();
    Serializable id();
}
