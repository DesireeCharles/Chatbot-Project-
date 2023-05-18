import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;

class ChatClient implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void run(){

        try{
            clientSocket = new Socket("localhost", 1999);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.print("Connection to server successful!\n");

            InputHandler inHandler = new InputHandler();
            Thread writeToSocket = new Thread(inHandler);
            writeToSocket.start();
    
            String messageIn;
            while ((messageIn = in.readLine()) != null) {
                System.out.println(messageIn);
            }
        } catch(IOException e){
            e.printStackTrace();
        }        
    }

    class InputHandler implements Runnable {

        @Override
        public void run(){
            
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (true){
                    String message = inReader.readLine();
                    out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.run();
    }
}
