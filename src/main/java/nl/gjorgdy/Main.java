package nl.gjorgdy;

import nl.gjorgdy.discord.Discord;

public class Main {
    public static Discord discord;

    public Main() {
        discord = new Discord();
    }

    public void start() {
        discord.start();
    }

    public static void main(String[] args) {

        System.out.println("Starting Mainframe...");

        new Main().start();
    }
}