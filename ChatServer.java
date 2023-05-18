import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable{

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
                //System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
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
            if (client != null){
                client.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try{
            done = true;
            pool.shutdown();
            if (!serverSocket.isClosed()){
                serverSocket.close();
            }
            for (ClientHandler client: clients){
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

        public ClientHandler(Socket socket){
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
                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(username + ": " + message);
                }
            } catch (IOException e) {
                shutdown();
            } 
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown(){
            try{
                in.close();
                out.close();
                if (!socket.isClosed()){
                    socket.close();
                }
            } catch (IOException e){shutdown();}
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.run();
    }
}