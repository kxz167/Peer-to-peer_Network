import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
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
        TimerTask heartbeat = new TimerTask() {

            @Override
            public void run() {
                try {

                    dos.writeUTF("Heartbeat");

                    System.out.println("Sending heartbeat to: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // System.out.println("Heartbeat Created");

        Long heartbeatInterval = 10000L;
        heartbeatTimer.scheduleAtFixedRate(heartbeat, heartbeatInterval, heartbeatInterval);

        // Waits for incoming responses
        while (true) {
            String input = "";
            // System.out.println("Waiting for response");
            try {
                input = dis.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                parseInput(input);
            } catch (IOException e) {

            }
        }
    }


    public void parseInput(String input) throws IOException {
        Scanner inputScanner = new Scanner(input);
        inputScanner.useDelimiter(":");

        System.out.println(input);

        String queryType = inputScanner.next();

        // System.out.println("Query type is: " + queryType);

        inputScanner.useDelimiter(";");

        // System.out.println("Trying to receive Query");

        inputScanner.skip(":");

        String queryID = inputScanner.next();
        // System.out.println("ID is:" + queryID);

        inputScanner.skip(";");

        if (queryType.equals("R")) {
            inputScanner.useDelimiter(":");
            System.out.println("Response received");

            String peerIP = inputScanner.next();
            inputScanner.skip(":");
            inputScanner.useDelimiter(";");

            int peerPort = inputScanner.nextInt();
            String filename = inputScanner.next();

            Query receivedQuery = new Query(true, queryID, peerIP, peerPort, filename,
                    this.socket.getInetAddress().getHostAddress());

            // Response received, is this response responded already?
            if (p2p.getResponseQueries().containsKey(queryID)) {
                // Already responded to so drop, do nothing
            } else {
                // if not, is it mine?
                if (p2p.isMine(receivedQuery)) {
                    // Its mine, lets start downloading
                    System.out.println("This is my request");
                    p2p.retreiveFile(receivedQuery.getFilename(), receivedQuery.getPeerIP(), receivedQuery.getPeerPort());
                }
                // if not, send to whoever sent the query
                else {
                    // Not mine, forward the response:
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
        // System.out.println("I am sending query");
        this.dos.writeUTF("Q:" + nextQuery.getID() + ";" + nextQuery.getFilename());
    }
}