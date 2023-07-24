package nl.gjorgdy.events;

import nl.gjorgdy.database.identifiers.Identifier;

import java.util.List;

public interface UserListener {

// User updates
    default void onUserDisplayNameUpdate(List<Identifier> userIdentifiers, String newDisplayName) {}

    default void onUserConnectionUpdate(List<Identifier> userIdentifiers, Identifier connection, boolean additive) {}

    default void onUserRoleUpdate(List<Identifier> userIdentifiers, List<Identifier> roleIdentifiers, boolean additive) {}

}
