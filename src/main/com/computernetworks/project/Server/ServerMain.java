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
                logger.info("Accepted connection from: " + connectionSocket.getInetAddress().getHostAddress() + "\n"); 

                // a new thread per client so we can keep accepting new ones
                new Thread(() -> handleClient(connectionSocket)).start();
            }
        }
    }

    // handles one client for its entire session
    private static void handleClient(Socket socket) {

        String addr = socket.getInetAddress().getHostAddress();
        long connectionTime = System.currentTimeMillis();
        String clientName = addr; 
        logger.info("Client connected | ClientName=" + clientName + " | IP=" + addr + "\n");

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
                    if (clientName.isEmpty()) {
                        clientName = addr;
                    }
                    logger.info("JOIN | ClientName=" + clientName + " | IP=" + addr + "\n");
                    out.println("ACK:" + clientName);

                } else if (line.startsWith("CALC:")) {
                    String expr = line.substring(5).trim();
                    logger.info("CALC | ClientName=" + clientName + " | Expression=" + expr + "\n");
                    
                    // actually evaluate the expression now
                    try {
                        double result = evaluateExpression(expr);
                        out.println("RESULT:" + result);
                        logger.info("RESULT | ClientName=" + clientName + " | Expression=" + expr + " | Answer=" + result + "\n");
                    } catch (Exception e) {
                        out.println("ERROR:Invalid expression! " + e.getMessage());
                        logger.warning("CALC ERROR | ClientName=" + clientName + " | Expression=" + expr + " | Error=" + e.getMessage() + "\n");
                    }

                } else if (line.equals("QUIT")) {
                    logger.info("QUIT | ClientName=" + clientName + " | IP=" + addr + "\n");
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
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Math expression evaluator
     * using the BODMAS order (Brackets, Orders (exponents), Division, Multiplication, Addition, Subtraction)
     * supports +, -, *, /, ^ and ()
     */
    private static double evaluateExpression(String inputExpression) throws Exception {

        // clean up the expression
        inputExpression = inputExpression.replaceAll("\\s+", "");  // remove spaces
        inputExpression = inputExpression.replace("÷", "/");       // division symbol
        inputExpression = inputExpression.replace("–", "-");       // en-dash to minus
        


        // handle implicit multiplication like "32(2)" or ")(3"
        // i know that this might be a bit unnecessary
        StringBuilder fixed = new StringBuilder();
        for (int i = 0; i < inputExpression.length(); i++) {
            char current = inputExpression.charAt(i);
            fixed.append(current);
            
            // if we're not at the last character, check if we need to insert *
            if (i < inputExpression.length() - 1) {
                char next = inputExpression.charAt(i + 1);
                // number followed by ( or ) followed by ( or ) followed by number
                if ((Character.isDigit(current) && next == '(') ||
                    (current == ')' && next == '(') ||
                    (current == ')' && Character.isDigit(next))) {
                    fixed.append('*');
                }
            }
        }
        inputExpression = fixed.toString();
        


        if (inputExpression.isEmpty()) {
            throw new Exception("empty expression");
        }
        
        //  Mismaching parentheses, finding innermost and evaluating recursively
        while (inputExpression.contains("(")) {
            int close = inputExpression.indexOf(')');

            if (close == -1) {
                throw new Exception("mismatched parentheses");
            }

            int open = inputExpression.lastIndexOf('(', close);
            String inside = inputExpression.substring(open + 1, close);

            double result = evaluateExpression(inside);
            inputExpression = inputExpression.substring(0, open) + result + inputExpression.substring(close + 1);
        }
        
        if (inputExpression.contains(")")) {
            throw new Exception("mismatched parentheses");
        }
        
        // split into parts (numbers and operators)
        java.util.List<String> inputExpressionParts = new java.util.ArrayList<>();
        StringBuilder number = new StringBuilder();
        
        for (int i = 0; i < inputExpression.length(); i++) {
            char c = inputExpression.charAt(i);
            if (Character.isDigit(c) || c == '.') {

                number.append(c);

            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                // negative number at start or after operator
                if (c == '-' && (i == 0 || "+-*/^".indexOf(inputExpression.charAt(i-1)) >= 0)) {
                    number.append(c);
                } else {
                    if (number.length() == 0) {
                        throw new Exception("invalid syntax");
                    }
                    inputExpressionParts.add(number.toString());
                    inputExpressionParts.add(String.valueOf(c));
                    number = new StringBuilder();
                }
            } else {
                throw new Exception("invalid character: " + c);
            }
        }
        if (number.length() > 0) {
            inputExpressionParts.add(number.toString());
        }
        
        // exponents (^) from left to right
        for (int i = 1; i < inputExpressionParts.size(); i += 2) {
            String operator = inputExpressionParts.get(i);
            
            if (operator.equals("^")) {
                double left = Double.parseDouble(inputExpressionParts.get(i - 1));
                double right = Double.parseDouble(inputExpressionParts.get(i + 1));
                double result = Math.pow(left, right);
                inputExpressionParts.set(i - 1, String.valueOf(result));
                inputExpressionParts.remove(i);
                inputExpressionParts.remove(i);
                i -= 2;
            }
        }
        
        // * and / from left to right
        for (int i = 1; i < inputExpressionParts.size(); i += 2) {
            String operator = inputExpressionParts.get(i);

            if (operator.equals("*") || operator.equals("/")) {
                double left = Double.parseDouble(inputExpressionParts.get(i - 1));
                double right = Double.parseDouble(inputExpressionParts.get(i + 1));
                if (operator.equals("/") && right == 0) {
                    throw new Exception("cannot divide by zero");
                }
                double result = operator.equals("*") ? left * right : left / right;
                inputExpressionParts.set(i - 1, String.valueOf(result));
                inputExpressionParts.remove(i);
                inputExpressionParts.remove(i);
                i -= 2;
            }
        }
        
        // + and - from left to right
        double result = Double.parseDouble(inputExpressionParts.get(0));

        for (int i = 1; i < inputExpressionParts.size(); i += 2) {
            String operator = inputExpressionParts.get(i);
            double next = Double.parseDouble(inputExpressionParts.get(i + 1));
            if (operator.equals("+")) {
                result += next;
            } else if (operator.equals("-")) {
                result -= next;
            }
        }
        
        return result;
    }
}