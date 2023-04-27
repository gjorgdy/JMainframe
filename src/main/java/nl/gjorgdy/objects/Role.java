package nl.gjorgdy.objects;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

public class Role {

    private final ObjectId roleId;
    private Role parentRole;
    private String displayName;

    public Role(ObjectId roleId, String displayName, @Nullable Role parentRole) {
        this.roleId = roleId;
        this.displayName = displayName;
        this.parentRole = parentRole;
    }

    public ObjectId getRoleId() {
        return roleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getParentRole() {
        return parentRole;
    }
}
