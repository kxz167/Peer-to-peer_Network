import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Scanner;

public class RequestSender extends RequestHandler {
    private Long BEAT_INTERVAL = 30000L;

    /**
     * Creates a new request sender which will send a request to the peer it is connected to
     * @param socket The socket this connection is utilizing to connect to the peer
     */
    public RequestSender(Socket socket) {
        super(socket);
    }

    /**
     * Override so that the requests can be sent to the peer without halting the main p2p program.
     */
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

        //While we are open, keep sending in a heartbeat into the socket.
        heartbeatTimer.scheduleAtFixedRate(heartbeat, BEAT_INTERVAL, BEAT_INTERVAL);

        // Waits for incoming responses
        while (open) {
            String input = "";

            try {
                input = dis.readUTF();

            } catch (IOException e) {
                // Silent personal exit, could utilize logger.
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

    /**
     * Creates a query based on the response protocol
     * @param input The string representation of the query response
     * @return Returns a new query that can be handled with the information from the input
     */
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

    /**
     * Handles the response that was received at the requestSender. Will decide if the response is forwarded or not.
     * @param receivedQuery The query that the peer has received.
     */
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

    /**
     * Send out a query request to the connected peer
     * @param nextQuery The query which is going to need to be sent out.
     */
    @Override
    public void sendQuery(Query nextQuery) {
        try {
            this.dos.writeUTF("Q:" + nextQuery.getID() + ";" + nextQuery.getFilename());
        } catch (IOException e) {
            System.out.println("Error while sending query");
        }
    }
}