package nl.gjorgdy.objects.connections;

public class MinecraftConnection implements Connection {

    private final String uuid;

    MinecraftConnection(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Type getType() {
        return Type.MINECRAFT_USER;
    }

    public String getUuid() {
        return uuid;
    }
}
