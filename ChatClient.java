import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;

class ChatClient implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader console;

    public void start(){

        try{
            clientSocket = new Socket("localhost", 1999);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            console = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Connection to server successful!\n");
    
            Thread writeToSocket = new Thread(this, "WriterThread");
            writeToSocket.start();
    
            String messageIn;
            while ((messageIn = in.readLine()) != null) {
                System.out.println("Received: " + messageIn);
            }
        } catch(IOException e){
            e.printStackTrace();
        }        
    }

    @Override
    public void run() {
        String input;
        try {
            while ((input = console.readLine()) != null) {
                out.println(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
