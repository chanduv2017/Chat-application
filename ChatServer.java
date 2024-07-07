import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Map<String, String> users = new HashMap<>();
    private static List<ClientThread> connectedClients = new ArrayList<>();
    private static FileWriter fileWriter;
    private static BufferedWriter bufferedWriter;

    public ChatServer() {
        users.put("user1", "password1");
        users.put("user2", "password2");
        // Add more username-password combinations as needed

        try {
            fileWriter = new FileWriter("chatlog.txt");
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Chat Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                ClientThread clientThread = new ClientThread(clientSocket);
                connectedClients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ClientThread extends Thread {
        private Socket clientSocket;
        private PrintWriter pw;
        private BufferedReader br;
        private String username;

        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                pw = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read the username from the client
                username = br.readLine();

                // Read the password from the client
                String password = br.readLine();

                // Check if the username and password combination exists in the user database
                if (users.containsKey(username) && users.get(username).equals(password)) {
                    pw.println("login_success");
                    System.out.println("User authenticated: " + username);

                    handleClientCommunication();
                } else {
                    pw.println("login_failed");
                    System.out.println("Invalid username or password: " + username);
                }

                // Remove the client from the connectedClients list
                connectedClients.remove(this);

                // Broadcast the user left message
                broadcastMessage(username + " left the chat.");

                // Close the file writer and buffered writer
                bufferedWriter.close();
                fileWriter.close();

                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void handleClientCommunication() throws IOException {
            // Handle the client communication here
            // You can implement the logic for sending/receiving messages, broadcasting, etc.

            // Example: Echo server that sends back received messages
            String message;
            while ((message = br.readLine()) != null) {
                pw.println("You sent: " + message);
                bufferedWriter.write(username + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientThread client : connectedClients) {
            client.pw.println(message);
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start(5200);
    }
}
