package nl.gjorgdy.objects.connections;

public class MinecraftConnection implements Connection {

    private final String uuid;

    MinecraftConnection(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getType() {
        return null;
    }

    public String getUuid() {
        return uuid;
    }
}
