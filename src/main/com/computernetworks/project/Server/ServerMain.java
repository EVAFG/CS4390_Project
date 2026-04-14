package com.computernetworks.project.Server;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    public static void main(String argv[]) throws Exception
    {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(6789);

        logger.info("Server is UP and running!");

        while(true) {      
            Socket connectionSocket = welcomeSocket.accept();
            logger.info("Accepted connection from: " + connectionSocket.getInetAddress().getHostAddress());
            
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();

            logger.info("Server received message: " + clientSentence);
            capitalizedSentence = clientSentence.toUpperCase() + '\n';
            logger.info("Server sending message: " + capitalizedSentence);
           outToClient.writeBytes(capitalizedSentence);
        }
        connectionSocket.close();
    }
}