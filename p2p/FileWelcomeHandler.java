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

    /**
     * Returns a handler to manage file connections welcome socket
     * @param port The port that the socket should be listening on
     */
    public FileWelcomeHandler(int port) {
        this.portNumber = port;
    }

    /**
     * Method to terminate the welcome handler and end the thread.
     */
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

    /**
     * Override to run the welcome handler on a separate thread with the given port.
     */
    @Override
    public void run() {
        // Create the server socket on the port
        try {    
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Could not create the FileWelcomePort");
        }

        //Keep accepting connections and creating a file sender.
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

    /**
     * A getter to return the port associated with the FileWelcomeHandler.
     * @return The port number for the fileWelcome handler used for responses.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }
}