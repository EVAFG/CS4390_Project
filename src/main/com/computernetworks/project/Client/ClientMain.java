package com.computernetworks.project.Client;
import java.io.*;
import java.net.*;

class ClientMain {

    public static void main(String argv[]) throws Exception
    {
        String sentence;
        String modifiedSentence;
        System.out.println("Client is running: " );

        Socket clientSocket = new Socket("localhost", 6789);
        BufferedReader inFromUser =
          new BufferedReader(new InputStreamReader(System.in));
        
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();

        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();
        
        System.out.println("FROM SERVER: " + modifiedSentence);
    }
}