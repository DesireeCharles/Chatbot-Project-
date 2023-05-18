import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
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
            while (!done) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);        
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
        } catch (IOException e) {
            shutdown();
        }
    }

    class ClientHandler implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private PrintWriter writer;
        private HttpClient httpClient;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("Pick your username: ");
                username = in.readLine();
                System.out.println(username + " connected!");
                broadcastMessage(username + " joined the chat!");
                String message = in.readLine();
                System.out.println(message);
                sendHttpRequest(message);
                // while ((message = in.readLine()) != null) {
                //     // System.out.println(message);
                //     sendHttpRequest(message);
                // }

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
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendHttpRequest(String message) {
            HttpClient client = HttpClient.newHttpClient();
            System.out.println("...Building...");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openai80.p.rapidapi.com/chat/completions"))
                    .header("content-type", "application/json")
                    .header("X-RapidAPI-Key", "KEY HERE!!!") 
                    .header("X-RapidAPI-Host", "openai80.p.rapidapi.com")
                    .POST(HttpRequest.BodyPublishers.ofString("{\r\n    \"model\": \"gpt-3.5-turbo\",\r\n \"messages\": [{\r\n \"role\": \"user\",\r\n \"content\": " +  message + " }]}"))
                    .build();

        
            try {
                System.out.println("Attempting post request");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response received");
                String aiResponse = "AI response: " + response.body();
                System.out.println(aiResponse);
                sendMessage(aiResponse); // Send the AI response back to the client
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
    }

    public static void main(String[] args) throws IOException,ClassNotFoundException{
        ChatServer chatServer = new ChatServer();
        chatServer.run();
    }
}
