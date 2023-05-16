package nl.gjorgdy.events;

import nl.gjorgdy.database.identifiers.Identifier;

public interface RoleListener {
    default void onRoleDisplayNameUpdate(Identifier[] roleIdentifiers, String displayName) {}
    default void onRoleParentUpdate(Identifier[] roleIdentifiers, Identifier[] parentRoleIdentifiers, boolean additive) {}
    default void onUserConnectionUpdate(Identifier[] roleIdentifiers, Identifier connection, boolean additive) {}
    default void onRolePermissionUpdate(Identifier[] roleIdentifiers, String permission, boolean additive) {}
}
