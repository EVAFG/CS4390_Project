/*
 * ServerMain.java - the math server
 */
package com.computernetworks.project.Server;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

class ServerMain {

    // one logger for the whole server
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    static final int PORT = 6789;

    public static void main(String[] args) throws Exception {
        try (ServerSocket welcomeSocket = new ServerSocket(PORT)) {
            logger.info("Server is UP and running on port " + PORT);

            while (true) {
                // accept() blocks until a client connects
                Socket connectionSocket = welcomeSocket.accept();
                logger.info("Accepted connection from: " + connectionSocket.getInetAddress().getHostAddress());

                // new thread per client so the loop can keep accepting
                new Thread(() -> handleClient(connectionSocket)).start();
            }
        }
    }

    // handles one client for its entire session (reads lines, sends them back uppercased for now)
    private static void handleClient(Socket socket) {

        String addr = socket.getInetAddress().getHostAddress();
        long connectionTime = System.currentTimeMillis();
        logger.info("Client connected | ip=" + addr);

        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                logger.info("[" + addr + "] received: " + line);
                out.println(line.toUpperCase());   // placeholder, math eval goes here later
            }
        } catch (IOException e) {
            logger.warning("error with " + addr + ": " + e.getMessage());
        } finally {
            // log duration before closing, how long did this client stay connected
            long seconds = (System.currentTimeMillis() - connectionTime) / 1000;
            logger.info("Client disconnected | ip=" + addr + " | duration=" + seconds + "s");
            
            // close the socket to free up resources, but ignore any errors since we're already done with it
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}