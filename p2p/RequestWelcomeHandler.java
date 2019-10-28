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

    private List<RequestReceiver> openConnections = new ArrayList<>();

    public RequestWelcomeHandler(int port) {
        this.portNumber = port;
    }

    public void terminate() {
        for (RequestReceiver connection : openConnections) {
            connection.terminate();
        }

        open = false;

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server socket closed");
        }

    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Error creating Request welcome socket");
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
                RequestReceiver newHandler = new RequestReceiver(clientSocket);

                System.out.println("Accepting connection from : " + clientSocket.getInetAddress().getHostAddress() + ":"
                        + clientSocket.getPort());

                newHandler.start();

                p2p.addIncoming(newHandler);
            }
        }
    }
}