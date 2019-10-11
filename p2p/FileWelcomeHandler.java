import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class FileWelcomeHandler extends Thread {
    private int portNumber;
    private ServerSocket serverSocket;
    private boolean open = true;
    // private List<Thread> clientSockets = new ArrayList<>();

    private List<FileSender> openConnections = new ArrayList<>();

    public FileWelcomeHandler(int port) {
        this.portNumber = port;
    }

    public void terminate ()throws IOException{
        for (FileSender connection : openConnections){
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

        while (open) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();

                FileSender newFileSender = new FileSender(clientSocket);

                newFileSender.start();
                openConnections.add(newFileSender);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}