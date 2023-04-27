package nl.gjorgdy.objects;

import java.io.Serializable;

public class Identifier implements Serializable {

    private final String platform;
    private final long longId;
    private final String stringId;

    public Identifier(String platform, long longId, String stringId) {
        this.platform = platform;
        this.longId = longId;
        this.stringId = stringId;
    }

    public String getPlatform() {
        return platform;
    }

    public long getLongId() {
        return longId;
    }

    public String getStringId() {
        return stringId;
    }
}
