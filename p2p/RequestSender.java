import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Scanner;

public class RequestSender extends RequestHandler {
    private Long BEAT_INTERVAL = 30000L;
    public RequestSender(Socket socket) {
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
                    System.out.println("Error writing to the socket");
                }
            }
        };

        heartbeatTimer.scheduleAtFixedRate(heartbeat, BEAT_INTERVAL, BEAT_INTERVAL);

        // Waits for incoming responses
        while (open) {
            String input = "";

            try {
                input = dis.readUTF();

            } catch (IOException e) {
                // Silent personal exit
                // System.out.println("Error reading from the socket");
            }
            switch (input) {
            case "Close":
                terminate();
                p2p.removeRequestSender(this);
                break;
            default:
                if (open) {
                    handleResponse(responseFrom(input));
                }
                break;
            }
        }
    }

    public Query responseFrom(String input) {

        Scanner inputScanner = new Scanner(input);

        // Current request structure: R:(query ID);(peerIP:port);(filename)
        inputScanner.skip("R:");

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

        inputScanner.close();

        return new Query(true, queryID, peerIP, peerPort, filename, this.socket.getInetAddress().getHostAddress());
    }

    public void handleResponse(Query receivedQuery) {
        String queryID = receivedQuery.getID();

        if (!p2p.seenResponse(queryID)) {
            p2p.addResponse(receivedQuery);
            
            // No, what next?
            if (p2p.responseForMe(queryID)) {
                // Its for me, lets start downloading
                p2p.retreiveFile(receivedQuery.getFilename(), receivedQuery.getPeerIP(), receivedQuery.getPeerPort());
            } else {
                // Not mine, forward the response:
                p2p.forwardResponseQuery(receivedQuery);
            }
        } else {
            // Already responded to so drop response
        }
    }

    @Override
    public void sendQuery(Query nextQuery) {
        try {
            this.dos.writeUTF("Q:" + nextQuery.getID() + ";" + nextQuery.getFilename());
        } catch (IOException e) {
            System.out.println("Error while sending query");
        }
    }

    @Override
    public void erase() {
        p2p.removeRequestSender(this);
    }
}