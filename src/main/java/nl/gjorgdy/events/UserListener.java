package nl.gjorgdy.events;

import nl.gjorgdy.chats.Message;
import nl.gjorgdy.database.identifiers.DatabaseIdentifier;
import nl.gjorgdy.database.identifiers.Identifier;

public interface UserListener {

// User updates
    default void onUserDisplayNameUpdate(Identifier[] userIdentifiers, String newDisplayName) {}

    default void onUserConnectionUpdate(Identifier[] userIdentifiers, Identifier connection, boolean additive) {}

    default void onUserRoleUpdate(Identifier[] userIdentifiers, Identifier[] roleIdentifiers, boolean additive) {}

// Message events
    default void onMessage(Message msg) {}


}
