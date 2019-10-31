import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class p2p {

    // Stores the peer hostname for repeated use
    private static String myHost = "";

    private static List<String> myFiles = new ArrayList<>();

    // Peer status booleans
    private static boolean running = true;
    private static boolean connected = false;

    private static RequestWelcomeHandler requestWelcomeHandler;
    private static FileWelcomeHandler fileWelcomeHandler;

    private static List<RequestHandler> peers = new ArrayList<>();
    private static Map<String, RequestHandler> incomingPeers = new TreeMap<>();

    // Something to hold queries
    private static Map<String, Query> requestQueries = new TreeMap<>();
    private static Map<String, Query> personalQueries = new TreeMap<>();
    private static Map<String, Query> responseQueries = new TreeMap<>();

    public static void main(String[] args) {

        // Set frequently accessed host name
        try {
            myHost = InetAddress.getByName(InetAddress.getLocalHost().getHostName() + ".case.edu").getHostAddress();
        } catch (IOException e) {
            System.out.println("Can't find this host address");
        }

        System.out.println("Starting host: " + myHost);

        // Initiation
        initWelcomeSockets();
        startWelcomeSockets();
        findMyFiles();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        // Take commands
        while (running) {
            try {
                parseInput(inputReader.readLine());
            } catch (IOException e) {
                System.out.println("Cannot read the line in from the user");
            }
        }

        try {
            inputReader.close();
        } catch (IOException e) {
            System.out.println("Error closing the inputReader");
        }
    }

    /**
     * Determines the command that is requested from the given string and triggers
     * action
     * 
     * @param input The string that tholds the user input
     */
    public static void parseInput(String input) {
        Scanner inputScanner = new Scanner(input);
        String command = inputScanner.next();

        command = command.toLowerCase();

        switch (command) {
        case "connect":
            if (!connected) {
                initNeighborConnections();
                startNeighborConnections();
                connected = true;
            }
            break;
        case "get":
            createQuery(inputScanner.next());
            break;
        case "leave":
            // Gracefully terminates the outgoing peer connections
            for (int i = 0; i < peers.size(); i++) {
                RequestHandler target = peers.get(i);
                target.terminate();
            }

            peers.clear();
            connected = false;

            break;
        case "exit":
            terminate();
            break;
        default:
            System.out.println("Unknown command, please try again");
        }

        inputScanner.close();
    }

    /**
     * Initiates the welcome sockets for both the files and requests. This takes in
     * the peers to connect to from the config_peer.txt file
     */
    public static void initWelcomeSockets() {
        File config_peers = new File("config_peer.txt");

        try {
            Scanner portScanner = new Scanner(config_peers);

            requestWelcomeHandler = new RequestWelcomeHandler(portScanner.nextInt());
            fileWelcomeHandler = new FileWelcomeHandler(portScanner.nextInt());

            portScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("The config_peer file is not found to establish peers");
        }
    }

    /**
     * After the welcome sockets are created, we need to start them. They will begin
     * listening after this
     */
    public static void startWelcomeSockets() {
        requestWelcomeHandler.start();
        fileWelcomeHandler.start();
    }

    /**
     * We need to connect to the neighbors that are defined in the neighbors txt
     * file. This will create a requestSender for each neighbor
     */
    public static void initNeighborConnections() {
        File config_neighbors = new File("config_neighbors.txt");

        Scanner neighborScanner = null;
        try {
            neighborScanner = new Scanner(config_neighbors);
        } catch (FileNotFoundException e) {
            System.out.println("The config_neighbors file is not found to establish neighbors");
        }

        if (neighborScanner != null) {
            while (neighborScanner.hasNext()) {
                //Information for the neighbor to connect to
                String ip = neighborScanner.next();
                int port = neighborScanner.nextInt();

                System.out.print("Attempting to connect to peer: " + ip + ":" + port + "... ");

                //Initiating connection
                try {
                    RequestHandler connection = new RequestSender(new Socket(ip, port));
                    System.out.println("Success");
                    peers.add(connection);
                } catch (Exception e) {
                    System.out.println("Failure");
                }
            }
            neighborScanner.close();
        }
    }

    /**
     * Starts the 
     */
    public static void startNeighborConnections() {
        for (RequestHandler handler : peers) {
            handler.start();
        }
    }

    public static void findMyFiles() {

        File config_sharing = new File("config_sharing.txt");

        try {
            Scanner fileScanner = new Scanner(config_sharing);

            while (fileScanner.hasNext()) {
                myFiles.add(fileScanner.next());
            }

            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Shared files file is not found");
        }
    }

    // QUERIES
    public static void floodPeers(Query nextQuery) {

        for (RequestHandler connection : peers) {
            connection.sendQuery(nextQuery);
        }

        System.out.println("Sending query: " + nextQuery.getID());

    }

    public static void createQuery(String filename) {

        String queryID = myHost + filename;
        Query nextQuery = new Query(false, queryID, filename);

        personalQueries.put(queryID, nextQuery);
        floodPeers(nextQuery);
    }

    public static void forwardRequestQuery(Query nextQuery) {
        requestQueries.put(nextQuery.getID(), nextQuery);

        floodPeers(nextQuery);
    }

    public static void forwardResponseQuery(Query response) {
        String destinationIP = requestQueries.get(response.getID()).getPreviousIP();

        incomingPeers.get(destinationIP).sendQuery(response);
    }

    // GETTERS
    public static String getIP() {
        return myHost;
    }

    public static int getFileSocketPort() {
        return fileWelcomeHandler.getPort();
    }

    // INFO GETTERS
    public static boolean hasFile(String filename) {
        return myFiles.contains(filename);
    }

    public static boolean seenRequest(String queryID) {
        return requestQueries.containsKey(queryID) || personalQueries.containsKey(queryID);
    }

    public static boolean seenResponse(String queryID) {
        return responseQueries.containsKey(queryID);
    }

    public static boolean responseForMe(String queryID) {
        return personalQueries.containsKey(queryID);
    }

    // ADDERS
    public static void addResponse(Query responseQuery) {
        responseQueries.put(responseQuery.getID(), responseQuery);
    }

    public static void addRequest(Query request) {
        requestQueries.put(request.getFilename(), request);
    }

    public static void addIncoming(RequestReceiver incomingPeer) {
        incomingPeers.put(incomingPeer.getIP(), incomingPeer);
    }

    // CLEANUP
    public static void removeRequestReceiver(String IP) {
        incomingPeers.remove(IP);
    }

    public static void removeRequestSender(RequestHandler deadThread) {
        peers.remove(deadThread);
    }

    public static void terminate() {
        requestWelcomeHandler.terminate();
        fileWelcomeHandler.terminate();

        // Close all peers
        for (RequestHandler connection : peers) {
            connection.terminate();
        }

        // Close all incoming peers
        for (Map.Entry<String, RequestHandler> entry : incomingPeers.entrySet()) {
            entry.getValue().terminate();
        }

        running = false;
    }

    // FILE RETREIVAL
    public static void retreiveFile(String filename, String ip, int port) {
        try {

            Socket fileSocket = new Socket(ip, port);

            FileReceiver fileReceiver = new FileReceiver(fileSocket, filename);
            fileReceiver.start();

        } catch (UnknownHostException e) {
            System.out.println("Can't find specified host");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

}