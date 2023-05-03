package nl.gjorgdy.chats;

import nl.gjorgdy.Main;
import nl.gjorgdy.database.records.ChannelRecord;
import nl.gjorgdy.database.records.UserRecord;

import java.io.*;

public class MessageHandler extends Thread {

    private final ObjectOutputStream messageOutputStream;
    private final ObjectInputStream messageInputStream;
    private ChannelRecord channelRecord;

    public MessageHandler(ChannelRecord channelRecord) throws IOException {
        this.channelRecord = channelRecord;
        // Create piped streams
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        // Create object streams
        messageOutputStream = new ObjectOutputStream(outputStream);
        messageInputStream = new ObjectInputStream(inputStream);
    }

    public void send(Message msg) {
        try {
            messageOutputStream.writeObject(msg);
        } catch (IOException e) {
            System.err.println("Could not send message : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        Message msg;
        while (true) {
            // Try handling the message
            try {
                msg = (Message) messageInputStream.readObject();
                // Get user
                UserRecord userRecord = Main.MONGODB.userHandler.get(msg.author_identifier());
                // Print message
                System.out.print("\n " + channelRecord.display_name() + " | " + userRecord.display_name() + " : " + msg.content());
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
