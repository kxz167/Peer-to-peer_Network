import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Scanner;

public class RequestSender extends RequestHandler {

    public RequestSender(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void run() {

        // Handles heartbeat sending
        heartbeat = new TimerTask() {
            @Override
            public void run() {
                try {
                    dos.writeUTF("Heartbeat");
                    System.out.println("Sending heartbeat to: " + socket.getInetAddress().getHostAddress() + ":"
                            + socket.getPort());

                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        };

        Long heartbeatInterval = 30000L;
        heartbeatTimer.scheduleAtFixedRate(heartbeat, heartbeatInterval, heartbeatInterval);

        // Waits for incoming responses
        while (open) {
            String input = "";

            try {
                input = dis.readUTF();
            } catch (IOException e) {
                // e.printStackTrace();
            }

            try {
                handleResponse(responseFrom(input));
            } catch (IOException e) {

            }
        }
    }

    public Query responseFrom(String input) throws IOException {

        Scanner inputScanner = new Scanner(input);

        // Current request structure: R:(query ID);(peerIP:port);(filename)
        inputScanner.useDelimiter(":");
        String queryType = inputScanner.next();
        inputScanner.skip(":");

        // Current request structure: (query ID);(peerIP:port);(filename)
        inputScanner.useDelimiter(";");
        String queryID = inputScanner.next();
        inputScanner.skip(";");

        // Current request structure: (peerIP:port);(filename)
        inputScanner.useDelimiter(":");
        String peerIP = inputScanner.next();
        inputScanner.skip(":");

        // Current request structure: port);(filename)
        inputScanner.useDelimiter(";");
        int peerPort = inputScanner.nextInt();

        String filename = inputScanner.next();

        return new Query(true, queryID, peerIP, peerPort, filename,
                this.socket.getInetAddress().getHostAddress());
    }

    public void handleResponse(Query receivedQuery) throws IOException{
        String queryID = receivedQuery.getID();

        if (!p2p.seenResponse(queryID)) {
            // No, what next?
            if (p2p.myResponse(queryID)) {
                // Its for me, lets start downloading
                p2p.retreiveFile(receivedQuery.getFilename(), receivedQuery.getPeerIP(), receivedQuery.getPeerPort());
            } else {
                // Not mine, forward the response:
                p2p.forwardResponseQuery(receivedQuery);
            }
            p2p.addResponseQuery(receivedQuery);
        } else {
            // Already responded to so drop response
        }
    }

    @Override
    public void sendQuery(Query nextQuery) throws IOException {
        this.dos.writeUTF("Q:" + nextQuery.getID() + ";" + nextQuery.getFilename());
    }

    @Override
    public void erase(){
        p2p.removeRequestSender(this);
    }
}