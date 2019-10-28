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

    public void terminate(){
        for (FileSender connection : openConnections) {
            connection.terminate();
        }
        open = false;

        try{
            serverSocket.close();
        }
        catch(IOException e){
            System.out.println("The serverSocket was closed.");
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Could not create the FileWelcomePort");
        }

        while (open) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                // Silent on accept interrupt
                // System.out.println("Could not accept the incoming connection");
            }

            if (clientSocket != null) {

                FileSender newFileSender = new FileSender(clientSocket);

                newFileSender.start();
                openConnections.add(newFileSender);

            }
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}