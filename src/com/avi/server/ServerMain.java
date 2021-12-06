package com.avi.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    
    final static int PORT = 8181;
    
    public static void main(String[] args) {
        
        try {
            @SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(PORT);
            
            while (true) {
                System.out.println("Ready to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted from " + clientSocket);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write("Hello World".getBytes());
                clientSocket.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
