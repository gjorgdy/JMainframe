package nl.gjorgdy;

import nl.gjorgdy.chats.MessageForwarder;
import nl.gjorgdy.database.MongoDB;
import nl.gjorgdy.discord.Discord;
import nl.gjorgdy.events.Listeners;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static Discord DISCORD;
    public static MongoDB MONGODB;
    public static Listeners LISTENERS;
    public static MessageForwarder MESSAGE_FORWARDER;

    public Main() throws IOException {
        // Create a database instance
        MONGODB = new MongoDB();
        // Create a Discord bot instance
        DISCORD = new Discord();
        // Create a Listener instance
        LISTENERS = new Listeners();
        // Create a Message Forwarder instance
        MESSAGE_FORWARDER = new MessageForwarder();
    }

    public void start() {
        // Start MongoDB thread
        MONGODB.start();
        // Start Discord thread
        DISCORD.start();
        // Start message forwarding thread
        MESSAGE_FORWARDER.start();
        // Test shit

        // Start cli on main thread
        cli();
    }

    public void stop() {
        MONGODB.close();

        DISCORD.shutdown();

        System.exit(0);
    }

    public void cli() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n >>> Mainframe <<< \n");

        while (true) {
            String in;
            try {
                in = scanner.nextLine();
            } catch (IllegalStateException e) {
                break;
            }

            String[] inArray = in.split(" ");
            switch (inArray[0]) {
                case "stop" -> stop();
                case "status" -> {
                    System.out.println(" Mainframe Status:");
                    System.out.println("  MongoDB: " + MONGODB.getStatus());
                    System.out.println("  Discord: " + MONGODB.getStatus());
                }
                default -> System.out.println(
                    "Invalid command : \"" + in + "\""
                );
            }
        }
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Starting Mainframe...");

        new Main().start();
    }
}