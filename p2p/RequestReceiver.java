import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RequestReceiver extends RequestHandler {

    private long TIMEOUT = 60000L;

    public RequestReceiver(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        // Heartbeat, set timer.
        refreshTimer();
        while (open) {
            String input = "";

            // Get input from other side
            try {
                input = dis.readUTF();
            } catch (IOException e) {
                // e.printStackTrace();
            }

            if (open) {
                switch (input) {
                case "Heartbeat":
                    System.out.println("Heartbeat received from: " + this.socket.getInetAddress().getHostAddress() + ":"
                            + socket.getPort());
                    refreshTimer();
                    break;
                case "Close":
                    terminate();
                    p2p.removeRequestReceiver(getIP());
                    break;
                case "":
                    // Catch when connection forcefuly disconnected
                    break;
                default:
                    // A request was received

                    handleRequest(requestFrom(input));

                    break;
                }
            }
        }
    }

    public void refreshTimer() {
        if (heartbeat != null)
            heartbeat.cancel();

        heartbeatTimer.purge();

        heartbeat = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Heartbeat timeout with: " + socket.getInetAddress().getHostAddress() + ":"
                        + socket.getPort() + ", closing connection");
                // Close the socket
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        };

        heartbeatTimer.schedule(heartbeat, TIMEOUT);
    }

    public Query requestFrom(String input) {

        System.out.println("Request Query Received from: " + this.socket.getInetAddress().getHostAddress() + ":"
                + this.socket.getPort());

        Scanner inputScanner = new Scanner(input);

        // Current request structure: Q:(query ID);(short string)
        inputScanner.skip("Q:");

        // Current request structure: (query ID);(short string)
        inputScanner.useDelimiter(";");
        String queryID = inputScanner.next();

        // Current request structure: ;(short string)
        String filename = inputScanner.next();

        return new Query(false, queryID, filename, socket.getInetAddress().getHostAddress());

    }

    public void handleRequest(Query receivedQuery) {
        String filename = receivedQuery.getFilename();

        // What is the next steps?
        if (!p2p.seenRequest(receivedQuery.getID())) {
            if (p2p.hasFile(filename)) {
                System.out.println("I have file: " + filename + ", Sending response.");

                receivedQuery.setPeerIP(p2p.getIP());
                receivedQuery.setPeerPort(p2p.getFileSocketPort());

                p2p.addResponse(receivedQuery); // TODO Can move this?

                sendQuery(receivedQuery);
            } else {
                System.out.println("I don't have file: " + filename + ", Forwarding request.");
                p2p.forwardRequestQuery(receivedQuery);
            }
        } else {
            System.out.println("Request Seen, dropping");
            // Request received before, drop.
        }
    }

    @Override
    public void sendQuery(Query nextQuery) {
        try {
            this.dos.writeUTF("R:" + nextQuery.getID() + ";" + nextQuery.getPeerIP() + ":" + nextQuery.getPeerPort()
                    + ";" + nextQuery.getFilename());
        } catch (IOException e) {
            System.out.println("Error sending out response query");
        }
    }

    @Override
    public void erase() {
        p2p.removeRequestReceiver(getIP());
    }

}