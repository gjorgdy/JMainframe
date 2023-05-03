package nl.gjorgdy.events;

import nl.gjorgdy.database.records.RoleRecord;
import nl.gjorgdy.database.records.identifiers.Identifier;

public interface RoleListener {
    default void onRoleDisplayNameUpdate(RoleRecord roleRecord) {}
    default void onRoleParentUpdate(RoleRecord roleRecord, RoleRecord parentRoleRecord, boolean additive) {}
    default void onRoleConnectionUpdate(RoleRecord roleRecord, Identifier connection, boolean additive) {}
    default void onRolePermissionUpdate(RoleRecord roleRecord, String permission, boolean additive) {}
}
