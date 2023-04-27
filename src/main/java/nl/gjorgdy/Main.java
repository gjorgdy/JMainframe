package nl.gjorgdy;

import nl.gjorgdy.database.MongoDB;
import nl.gjorgdy.discord.Discord;
import nl.gjorgdy.objects.Message;
import nl.gjorgdy.objects.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static Discord DISCORD;
    public static MongoDB MONGO_DB;
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
        MONGO_DB = new MongoDB();
        // Create a Discord bot instance
        DISCORD = new Discord();
    }

    public void start() {
        // Start MongoDb thread
        MONGO_DB.start();
        // Start Discord thread
        DISCORD.start();
        // Database check
        while (!MONGO_DB.isReady()) {
            System.out.print(".");
        }
        User testUser = MONGO_DB.insertUser(
            new User(
                null,
                "testUser",
                new ArrayList<>(),
                new HashMap<>()
            )
        );
        System.out.println(testUser.getUserId());
        System.out.println(testUser.getDisplayName());
        // Start message forwarding thread
        messageForwarder();
    }

    public void messageForwarder() {
        Message msg;
        while (true) {
            try {
                msg = (Message) MESSAGE_INPUT_STREAM.readObject();
                System.out.println(msg.getAuthorIdentifier() + " : " + msg.getContent());
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