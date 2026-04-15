/*
 * ClientUI.java - Automated Demo Client
 *
 * CS 4390 - Computer Networks, Spring 2026
 * Project 1
 *
 * This client automatically sends 3  math requests to the server
 * at random intervals (1-3 seconds apart), then disconnects.
 *
 * Useful for demonstrating that two clients can connect simultaneously.
 *
 * Usage:
 *   java ClientUI <clientName> [serverIP] [port]
 *   Example: java ClientUI Alice
 *            java ClientUI Bob 192.168.1.10 6789
 */

package com.computernetworks.project.Client;

import java.io.*;
import java.net.*;
import java.util.Random;

public class ClientUI {

    static final int    DEFAULT_PORT = 6789;
    static final String DEFAULT_HOST = "localhost";

    // The 3+ math requests this automated client will send (per project requirement)
    private static final String[] REQUESTS = {
        "25 + 17",
        "100 / 4",
        "6 * 7",
        "200 - 83"
    };

    public static void main(String[] args) throws Exception {

        String clientName = args.length > 0 ? args[0] : "AutoClient";
        String host       = args.length > 1 ? args[1] : DEFAULT_HOST;
        int    port       = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_PORT;

        Random rand = new Random();

        System.out.println("[" + clientName + "] Connecting to " + host + ":" + port + " ...");

        try (
            Socket socket               = new Socket(host, port);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    outToServer  = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("[" + clientName + "] Connected.");

            // JOIN
            outToServer.println("JOIN:" + clientName);
            String ack = inFromServer.readLine();

            if (ack != null && ack.startsWith("ACK:")) {
                System.out.println("[" + clientName + "] Server acknowledged connection.");
            } else {
                System.out.println("[" + clientName + "] Unexpected JOIN response: " + ack);
                return;
            }

            // CALC requests at random intervals 
            for (String expr : REQUESTS) {
                // Random delay between 1000ms and 3000ms
                int delay = 1000 + rand.nextInt(2000);
                System.out.println("[" + clientName + "] Waiting " + delay + "ms before next request...");
                Thread.sleep(delay);

                outToServer.println("CALC:" + expr);
                System.out.println("[" + clientName + "] Sent: CALC:" + expr);

                String response = inFromServer.readLine();
                if (response == null) {
                    System.out.println("[" + clientName + "] Connection lost.");
                    return;
                }

                if (response.startsWith("RESULT:")) {
                    System.out.println("[" + clientName + "] " + expr + " = " + response.substring(7));
                } else if (response.startsWith("ERROR:")) {
                    System.out.println("[" + clientName + "] Error: " + response.substring(6));
                } else {
                    System.out.println("[" + clientName + "] Server: " + response);
                }
            }

            // QUIT
            outToServer.println("QUIT");
            String bye = inFromServer.readLine();
            if (bye != null) {
                System.out.println("[" + clientName + "] " + bye);
            }
            System.out.println("[" + clientName + "] Disconnected.");
        }
    }
}
