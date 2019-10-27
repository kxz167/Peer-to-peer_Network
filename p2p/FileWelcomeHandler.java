import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileWelcomeHandler extends Thread {
    private int portNumber;
    private ServerSocket serverSocket;
    private boolean open = true;

    private List<FileSender> openConnections = new ArrayList<>();

    public FileWelcomeHandler(int port) {
        this.portNumber = port;
    }

    public void terminate() throws IOException {
        for (FileSender connection : openConnections) {
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
        }

        while (open) {
            Socket clientSocket;
            try {
                if (open) {
                    clientSocket = serverSocket.accept();

                    // System.out.println("Accepting connection");
                    FileSender newFileSender = new FileSender(clientSocket);

                    // System.out.println("Start filesender");
                    newFileSender.start();
                    openConnections.add(newFileSender);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
            }

        }
    }

    public String getIP(){
        return serverSocket.getInetAddress().getHostAddress();
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }
}