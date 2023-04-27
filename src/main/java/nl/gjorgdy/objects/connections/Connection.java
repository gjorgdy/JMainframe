package nl.gjorgdy.objects.connections;

public interface Connection {

    static enum Type {
        DISCORD_USER,
        DISCORD_ROLE,
        MINECRAFT_USER
    }

    Type getType();

}
