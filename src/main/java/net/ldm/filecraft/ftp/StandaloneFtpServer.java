package net.ldm.filecraft.ftp;

import ca.weblite.objc.Runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class StandaloneFtpServer {
    public static void main(String[] args) {
        int port = 21;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FTP Server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            writer.println("220 FTP Server Ready");

            String input;
            while ((input = reader.readLine()) != null) {
                System.out.println("Received: " + input);

                // Implement FTP commands here
                if (input.equalsIgnoreCase("QUIT")) {
                    writer.println("221 Goodbye");
                    break;
                } else {
                    writer.println("500 Unknown command");
                }
            }

            clientSocket.close();
            System.out.println("Connection closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
