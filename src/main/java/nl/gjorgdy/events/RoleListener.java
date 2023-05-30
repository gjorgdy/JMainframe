package nl.gjorgdy.events;

import nl.gjorgdy.database.identifiers.Identifier;

import java.util.List;

public interface RoleListener {
    default void onRoleDisplayNameUpdate(List<Identifier> roleIdentifiers, String displayName) {}
    default void onRoleParentUpdate(List<Identifier> roleIdentifiers, List<Identifier> parentRoleIdentifiers, boolean additive) {}
    default void onRoleConnectionUpdate(List<Identifier> roleIdentifiers, Identifier connection, boolean additive) {}
    default void onRolePermissionUpdate(List<Identifier> roleIdentifiers, String permission, boolean additive) {}
}
