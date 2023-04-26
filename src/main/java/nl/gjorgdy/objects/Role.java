package nl.gjorgdy.objects;

import org.jetbrains.annotations.Nullable;

public class Role {

    public final long roleId;
    public String displayName;

    public Role(long roleId, String displayName, @Nullable Role parentRole) {
        this.roleId = roleId;
        this.displayName = displayName;
    }

}
