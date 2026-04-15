/*
 * ServerMain.java - Math Server
 *
 * CS 4390 - Computer Networks, Spring 2026
 * Project
 */
package com.computernetworks.project.Server;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

class ServerMain {

    // one logger for the whole server
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    static final int PORT = 6789; // default port, both client and server need to use the same one

    public static void main(String[] args) throws Exception {
        try (ServerSocket welcomeSocket = new ServerSocket(PORT)) {
            logger.info("Server is UP and running on port " + PORT);

            while (true) {
                // accept() blocks until a client connects
                Socket connectionSocket = welcomeSocket.accept();
                
                // log the client's IP address
                logger.info("Accepted connection from: " + connectionSocket.getInetAddress().getHostAddress()); 

                // a new thread per client so we can keep accepting new ones
                new Thread(() -> handleClient(connectionSocket)).start();
            }
        }
    }

    // handles one client for its entire session
    private static void handleClient(Socket socket) {

        String addr = socket.getInetAddress().getHostAddress();
        long connectionTime = System.currentTimeMillis();
        String clientName = addr; // will get replaced with the real name once JOIN comes in
        logger.info("Client connected | ClientName=" + clientName + " | IP=" + addr);

        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
) { 
            String line;
            while ((line = in.readLine()) != null) {

                line = line.trim();
                logger.info("[" + clientName + "] received: " + line);

                // dispatch based on message prefix (same idea as the TicTacToe server on cs.lmu.edu)
                if (line.startsWith("JOIN:")) {
                    clientName = line.substring(5).trim();
                    if (clientName.isEmpty()) clientName = addr;
                    logger.info("JOIN | ClientName=" + clientName + " | IP=" + addr);
                    out.println("ACK:" + clientName);

                } else if (line.startsWith("CALC:")) {
                    String expr = line.substring(5).trim();
                    logger.info("CALC | ClientName=" + clientName + " | Expression=" + expr);
                    out.println("ERROR:Math evaluator not yet implemented"); // TODO

                } else if (line.equals("QUIT")) {
                    logger.info("QUIT | ClientName=" + clientName + " | IP=" + addr);
                    out.println("BYE:" + clientName);
                    break;

                } else {
                    out.println("ERROR:Unknown command");
                }
            }
        } catch (IOException e) {
            logger.warning("error with " + addr + ": " + e.getMessage());
        } finally {
            // log how long they were connected before we close out
            long seconds = (System.currentTimeMillis() - connectionTime) / 1000;
            logger.info("Client disconnected | ClientName=" + clientName + " | IP=" + addr + " | Duration=" + seconds + "s");
            
            // close the socket to free up resources, but ignore any errors since we're already done with it
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}