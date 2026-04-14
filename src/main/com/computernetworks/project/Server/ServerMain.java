/*
 * ServerMain.java - the math server
 *
 * Listens on PORT for client connections. Each client gets its own thread
 * so multiple clients can be served at the same time.
 */
package com.computernetworks.project.Server;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    static final int PORT = 6789;

    public static void main(String[] args) throws Exception {
        try (ServerSocket welcomeSocket = new ServerSocket(PORT)) {
            logger.info("Server is UP and running on port " + PORT);

            while (true) {
                // accept() blocks until a client connects
                Socket connectionSocket = welcomeSocket.accept();
                logger.info("Accepted connection from: " + connectionSocket.getInetAddress().getHostAddress());

                // hand the socket off to a new thread so the loop can accept the next client immediately
                new Thread(() -> handleClient(connectionSocket)).start();
            }
        }
    }

    // handles one client for its entire session - reads lines, sends them back uppercased for now
    private static void handleClient(Socket socket) {
        String addr = socket.getInetAddress().getHostAddress();
        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                logger.info("[" + addr + "] received: " + line);
                out.println(line.toUpperCase());   // placeholder - math eval goes here later
            }
        } catch (IOException e) {
            logger.warning("error with " + addr + ": " + e.getMessage());
        } finally {
            // close here since the socket wasn't opened in a try-with-resources
            try { socket.close(); } catch (IOException ignored) {}
            logger.info("connection closed: " + addr);
        }
    }
}