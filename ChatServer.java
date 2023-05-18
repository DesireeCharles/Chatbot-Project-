import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatServer implements Runnable {

    private ArrayList<ClientHandler> clients;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private boolean done;
    private MessagePipe messagePipe;

    public ChatServer() {
        clients = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(1999);
            pool = Executors.newCachedThreadPool();
            System.out.println("Chat Server active");
            messagePipe = new MessagePipe(clients);
            messagePipe.startListening();
            while (!done) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);        
                clientHandler.setMessagePipe(messagePipe);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            if (client != null) {
                client.sendMessage(message);
            }
        }

    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.shutdown();
            }
            messagePipe.close(); // Close the message pipe
        } catch (IOException e) {
            shutdown();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.run();
    }

    public int getUserCount() {

        return 0;
    }

    public void connectUser(String Desiree) {
    }

    public void disconnectUser(String Desiree) {

    }

    public boolean MessageSent(String message) {
        return false;
    }



    class ClientHandler implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private MessagePipe messagePipe;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void setMessagePipe(MessagePipe messagePipe) {
            this.messagePipe = messagePipe;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("Pick your username: ");
                username = in.readLine();
                System.out.println(username + " connected!");
                messagePipe.writeMessage(username + " joined the chat!");
                broadcastMessage(username + " joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    messagePipe.writeMessage(username + ": " + message);
                    System.out.println(message);
                    sendHttpRequest(message);
                    broadcastMessage(username + ": " + message); // Broadcast the message to all clients
                }

            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

public void sendHttpRequest(String message) {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://chatgpt53.p.rapidapi.com/"))
            .header("content-type", "application/json")
            .header("X-RapidAPI-Key", "api")
            .header("X-RapidAPI-Host", "chatgpt53.p.rapidapi.com")
            .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    \"messages\": [\r\n        {\r\n            \"role\": \"user\",\r\n            \"content\": \"" + message + "\"   }\r\n    ]\r\n}"))
            .build();

    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        System.out.println("Response received");

        // Extract and print the AI response
        String aiResponse = "AI response: " + extractAiResponse(response.body());
        System.out.println(aiResponse);
        sendMessage(aiResponse); // Send the AI response back to the client
    } catch (Exception e) {
        e.printStackTrace();
    }
}

   private String extractAiResponse(String responseBody) {
            try {
                int startIndex = responseBody.indexOf("\"content\":\"") + 11;
                int endIndex = responseBody.indexOf("\"", startIndex);
                return responseBody.substring(startIndex, endIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
    }

    }

    class MessagePipe {

        private ArrayList<ClientHandler> clients;
        private FileWriter fileWriter;

        public MessagePipe(ArrayList<ClientHandler> clients) {
            this.clients = clients;
            try {
                fileWriter = new FileWriter("messages.txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeMessage(String message) {
            try {
                fileWriter.write(message + "\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void startListening() {
            for (ClientHandler client : clients) {
                client.setMessagePipe(this);
            }
        }

        public void close() {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
