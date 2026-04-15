/*
 * ClientMain.java - Math Server Client
 *
 * CS 4390 - Computer Networks, Spring 2026
 * Project 1
 *
 * Usage:
 *   java ClientMain [serverIP] [port]
 *   Defaults: localhost, 6789
 *
 * Protocol:
 *   JOIN:<n>         - sent on startup; waits for ACK:<n> before proceeding
 *   CALC:<expression>   - send a math expression; prints server's RESULT or ERROR
 *   QUIT                - sent on exit; waits for BYE then closes
 *
 * The client can send as many CALC requests as the user wants.
 * Type "exit" or "quit" to disconnect.
 */

package com.computernetworks.project.Client;

import java.io.*;
import java.net.*;

public class ClientMain {

    static final int DEFAULT_PORT = 6789;
    static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) throws Exception {

        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int    port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("=== Math Server Client ===");
        System.out.println("Connecting to " + host + ":" + port + " ...");

        try (
            Socket socket              = new Socket(host, port);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    outToServer  = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inFromUser   = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected.");

            //  Step 1: JOIN ──────────────────────────────────────────────────────────────
            System.out.print("Enter your name: ");
            String name = inFromUser.readLine().trim();
            if (name.isEmpty()) name = "Anonymous";

            outToServer.println("JOIN:" + name);

            // Wait for server acknowledgement before doing anything else
            String ack = inFromServer.readLine();
            if (ack != null && ack.startsWith("ACK:")) {
                System.out.println("Server acknowledged: welcome, " + ack.substring(4) + "!");
            } else {
                System.out.println("Unexpected response from server: " + ack);
                System.out.println("Exiting.");
                return;
            }

            //  Step 2: CALC loop ─────────────────────────────────────────────────────────
            System.out.println("\nYou can now send math expressions (e.g. 10 + 5, 8 * 3, 100 / 4).");
            System.out.println("Type 'exit' or 'quit' to disconnect.\n");

            String userInput;
            while (true) {
                System.out.print(name + "> ");
                userInput = inFromUser.readLine();

                if (userInput == null) break;  // stdin closed (e.g. piped input ended)
                userInput = userInput.trim();

                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    break;
                }

                if (userInput.isEmpty()) {
                    System.out.println("  (enter a math expression or 'quit')");
                    continue;
                }

                // Send the calculation request
                outToServer.println("CALC:" + userInput);

                // Wait for and display the server's answer
                String response = inFromServer.readLine();
                if (response == null) {
                    System.out.println("Connection lost.");
                    return;
                }

                if (response.startsWith("RESULT:")) {
                    System.out.println("  = " + response.substring(7));
                } else if (response.startsWith("ERROR:")) {
                    System.out.println("  Server error: " + response.substring(6));
                } else {
                    System.out.println("  Server: " + response);
                }
            }

            //  Step 3: QUIT ──────────────────────────────────────────────────────────────
            outToServer.println("QUIT");
            String bye = inFromServer.readLine();
            if (bye != null && bye.startsWith("BYE:")) {
                System.out.println("\nServer: Goodbye, " + bye.substring(4) + "!");
            }
            System.out.println("Disconnected. Bye!");
        }
    }
}
