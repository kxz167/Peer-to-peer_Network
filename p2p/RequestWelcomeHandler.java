import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RequestWelcomeHandler extends Thread {
    private int portNumber;
    private ServerSocket serverSocket;
    private boolean open = true;
    // private List<Thread> clientSockets = new ArrayList<>();

    private List<RequestReceiver> openConnections = new ArrayList<>();

    public RequestWelcomeHandler(int port) {
        this.portNumber = port;
    }

    public void terminate ()throws IOException{
        for (RequestReceiver connection : openConnections){
            connection.terminate();
        }
        
        open = false;
        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(open){
            try {
                Socket clientSocket = serverSocket.accept();
                
                RequestReceiver newHandler = new RequestReceiver(clientSocket);
                
                newHandler.start();
                openConnections.add(newHandler); 
            }
            
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public List<RequestReceiver> getConnections(){
        return this.openConnections;
    }
}