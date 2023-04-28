package nl.gjorgdy;

import nl.gjorgdy.database.MongoDB;
import nl.gjorgdy.database.records.Role;
import nl.gjorgdy.database.records.User;
import nl.gjorgdy.database.records.identifiers.Identifier;
import nl.gjorgdy.database.records.identifiers.StringIdentifier;
import nl.gjorgdy.discord.Discord;
import org.bson.types.ObjectId;

import java.io.*;

public class Main {
    public static Discord DISCORD;
    public static MongoDB MONGODB;
    public static ObjectOutputStream MESSAGE_OUTPUT_STREAM;
    private final ObjectInputStream MESSAGE_INPUT_STREAM;

    public Main() throws IOException {
        // Create piped streams
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        // Create object streams
        MESSAGE_OUTPUT_STREAM = new ObjectOutputStream(outputStream);
        MESSAGE_INPUT_STREAM = new ObjectInputStream(inputStream);
        // Create a database instance
        MONGODB = new MongoDB();
        // Create a Discord bot instance
        DISCORD = new Discord();
    }

    public void start() {
        // Start MongoDB thread
        MONGODB.start();
        // Start Discord thread
        DISCORD.start();
        // Database check/
        while (MONGODB.isNotReady()) {
            System.out.print(".");
        }
        User testUser = MONGODB.testInsertUser(
            new User(
                null,
                "Jordy",
                new Role[] {
                    new Role(new ObjectId(), "testRole", null, null),
                    new Role(new ObjectId(), "testRole2", null, null)
                },
                new Identifier[] {
                    new StringIdentifier(Identifier.Types.minecraft_user, "68b18dc4-9e72-4da2-912c-a2e3af5b6299")
                }
            )
        );
        System.out.println(String.join("\n",testUser.toStringList()) );
        // Start message forwarding thread
        messageForwarder();
    }

    public void messageForwarder() {
        Message msg;
        while (true) {
            try {
                msg = (Message) MESSAGE_INPUT_STREAM.readObject();
                System.out.println(msg.authorIdentifier() + " : " + msg.content());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Starting Mainframe...");

        new Main().start();
    }
}