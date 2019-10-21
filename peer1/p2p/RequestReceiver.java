import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RequestReceiver extends RequestHandler {

    private long TIMEOUT = 20000L;
    private boolean open = true;

    private TimerTask heartbeat = new TimerTask() {

        @Override
        public void run() {
            // Close the socket
            try {
                socket.close();
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    };

    public RequestReceiver(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void run() {
        // Handles heartbeat receiving. Close if none received in ____
        heartbeatTimer.schedule(heartbeat, TIMEOUT);

        while (open) {
            String input = "";
            System.out.println("Trying to read input");
            try {
                input = dis.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Scanner inputScanner = new Scanner(input);

            switch (input) {
            case "Heartbeat":
                System.out.println("Heartbeat received");
                refreshTimer();
                break;
            case "Close":
                System.out.println("Peer was closed");
                open = false;// Close the socket
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Other input received");
                try {
                    parseInput(input);
                } catch (IOException e) {

                }
                break;
            }
        }

        // Gets input requests

        //
    }

    public void refreshTimer() {
        heartbeatTimer.cancel();

        TimerTask newHeartbeat = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Heartbeat missed, closing socket");
                // Close the socket
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        };

        heartbeatTimer = new Timer();
        heartbeatTimer.schedule(newHeartbeat, TIMEOUT);
    }

    public void parseInput(String input) throws IOException {
        Scanner inputScanner = new Scanner(input);
        inputScanner.useDelimiter(":");

        System.out.println(input);

        String queryType = inputScanner.next();

        System.out.println("Query type is: " + queryType);

        inputScanner.useDelimiter(";");

        System.out.println("Trying to receive Query");

        inputScanner.skip(":");

        String queryID = inputScanner.next();
        System.out.println("ID is:" + queryID);

        if (queryType.equals("Q")) {
            System.out.println("Request Received");
            String filename = inputScanner.next();

            Query receivedQuery = new Query(false, queryID, filename, socket.getInetAddress().getHostAddress());

            if (p2p.hasFile(filename) && !p2p.getPersonalResponses().containsKey(queryID)) {
                // Return response
                System.out.println("I have file: " + filename);
                receivedQuery.setPeerIP(p2p.getFileSocketIP());
                receivedQuery.setPeerPort(p2p.getFileSocketPort());
                sendQuery(receivedQuery);
                p2p.getPersonalResponses().put(queryID, receivedQuery);
            } else if (!p2p.getRequestQueries().containsKey(queryID)) {
                // Not forwarded before, forward query
                p2p.forwardRequestQuery(receivedQuery);
            } else {
                // Forwarded before, drop query
            }

        } else if (queryType.equals("R")) {
            System.out.println("Response received");

            String peerIP = inputScanner.next();
            inputScanner.useDelimiter(";");

            int peerPort = inputScanner.nextInt();
            String filename = inputScanner.next();

            Query receivedQuery = new Query(true, queryID, peerIP, peerPort, filename,
                    this.socket.getInetAddress().getHostAddress());

            // Response received, is this response responded already?
            if (p2p.getResponseQueries().containsKey(queryID)) {
                //Already responded to so drop, do nothing
            } else {
                // if not, is it mine?
                if (p2p.isMine(receivedQuery)) {
                    //Its mine, lets start downloading
                    System.out.println("This is my request");
                }
                // if not, send to whoever sent the query
                else {
                    //Not mine, forward the response:
                    System.out.println("This is not my request, forwarding");
                    p2p.forwardResponseQuery(receivedQuery);
                }
                // Added to responded
                p2p.getResponseQueries().put(queryID, receivedQuery);
            }
        }
    }

    @Override
    public void sendQuery(Query nextQuery) throws IOException {
        System.out.println(this + ": I am sending response");
        this.dos.writeUTF("R:" + nextQuery.getID() + ";" + nextQuery.getPeerIP() + ":" + nextQuery.getPeerPort() + ";"
                + nextQuery.getFilename());
    }

}