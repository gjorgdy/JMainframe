package nl.gjorgdy;

import nl.gjorgdy.database.MongoDB;
import nl.gjorgdy.database.handlers.ChannelHandler;
import nl.gjorgdy.database.handlers.RoleHandler;
import nl.gjorgdy.database.handlers.ServerHandler;
import nl.gjorgdy.database.handlers.UserHandler;
import nl.gjorgdy.discord.Discord;
import nl.gjorgdy.events.handlers.Events;
import nl.gjorgdy.events.OldEvents;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Mainframe {
// Database
    private final MongoDB mongoDB;
    public static ChannelHandler CHANNELS;
    public static RoleHandler ROLES;
    public static ServerHandler SERVERS;
    public static UserHandler USERS;
// Event listeners
    public static OldEvents OldEvents;
    public static Events EVENT_HANDLERS;
// Modules
    private final Discord discord;
    // Todo websocket module

    public static Logger logger = new Logger("Mainframe");

    public Mainframe() throws IOException {
        // Create a Listener instance
        OldEvents = new OldEvents();
        EVENT_HANDLERS = new Events();
        // Create a database instance
        mongoDB = new MongoDB();
        CHANNELS = mongoDB.channelHandler;
        ROLES = mongoDB.roleHandler;
        SERVERS = mongoDB.serverHandler;
        USERS = mongoDB.userHandler;
        // Create a Discord bot instance
        discord = new Discord();
    }

    public void start() {
        // Start Discord thread
        discord.start();
        // Start cli on main thread
        cli();
    }

    public void stop() {
        mongoDB.shutdown();

        discord.shutdown();

        System.exit(0);
    }

    public void cli() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String in;
            String[] inArray;
            try {
                in = scanner.nextLine();
                inArray = in.split(" ");
            } catch (IllegalStateException | NoSuchElementException e) {
                break;
            }
            switch (inArray[0].toLowerCase()) {
            // Status of all modules
                case "status" -> System.out.println(
                    "Mainframe Status:"
                    + "\n- Discord Bot  |  " + discord.getStatus()
                    + "\n- Websocket    |  " + "not implemented"
                );
            // List commands
                case "help" -> System.out.println(
                    """
                        Mainframe Commands:
                        - help         |   this command
                        - status       |   status of all modules
                        - stop         |   stop server
                    """
                );
            // Stop the mainframe
                case "stop" -> stop();
            // Invalid command
                default -> System.out.println(
                    "Invalid command : \"" + in + "\", use \"help\" for a list of commands"
                );
            }
        }
        System.err.println("CLI is not supported on this device, shutting down module");
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Starting Mainframe...");

        new Mainframe().start();
    }
}