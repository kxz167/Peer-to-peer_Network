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
                // Information for the neighbor to connect to
                String ip = neighborScanner.next();
                int port = neighborScanner.nextInt();

                System.out.print("Attempting to connect to peer: " + ip + ":" + port + "... ");

                // Initiating connection
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
     * Starts the nighbor connections after they have been created
     */
    public static void startNeighborConnections() {
        for (RequestHandler handler : peers) {
            handler.start();
        }
    }

    /**
     * Searches through the config_sharing.txt file and finds all the files that the
     * current peer can hold. Put these names into a list
     */
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

    /**
     * Takes a query and sends it to all the peers that the current one has
     * connected to.
     * 
     * @param nextQuery The next query to be sent into the network.
     */
    public static void floodPeers(Query nextQuery) {

        for (RequestHandler connection : peers) {
            connection.sendQuery(nextQuery);
        }

        System.out.println("Sending query: " + nextQuery.getID());

    }

    /**
     * Takes a filename that the peer wants and creates a new one to flood into the
     * network
     * 
     * @param filename The filename that was requested by this peer
     */
    public static void createQuery(String filename) {

        // Create the ID as the host + the filename requested
        String queryID = myHost + filename;
        Query nextQuery = new Query(false, queryID, filename);

        personalQueries.put(queryID, nextQuery);
        floodPeers(nextQuery);
    }

    /**
     * This peer, if it does not have the file requested, will forward a query that
     * it has received from another peer i.
     * 
     * @param nextQuery The query to be forwarded into the network
     */
    public static void forwardRequestQuery(Query nextQuery) {
        requestQueries.put(nextQuery.getID(), nextQuery);

        floodPeers(nextQuery);
    }

    /**
     * When this peer receives a response to a request, but is not the peer which
     * requested the file, it will forward the response.
     * 
     * @param response The response to a query that was at this peer at one point
     *                 (if file is in network)
     */
    public static void forwardResponseQuery(Query response) {
        String destinationIP = requestQueries.get(response.getID()).getPreviousIP();

        incomingPeers.get(destinationIP).sendQuery(response);
    }

    /**
     * Returns the location of this peer.
     * 
     * @return The string representation of this peer's address
     */
    public static String getIP() {
        return myHost;
    }

    /**
     * Queries the file welcome handler for it's port
     * 
     * @return The int portnumber for this peer's file welcome socket.
     */
    public static int getFileSocketPort() {
        return fileWelcomeHandler.getPort();
    }

    /**
     * Will respond as to whether this peer has the requested file
     * 
     * @return True if the peer has the requested file, false otherwise.
     */
    public static boolean hasFile(String filename) {
        return myFiles.contains(filename);
    }

    /**
     * Determines whether this peer has already handled a received request
     * 
     * @param queryID The ID for the request query the peer is wondering about
     * @return True if the query has already been handled by this peer, false if the
     *         query has never been encounterd.
     */
    public static boolean seenRequest(String queryID) {
        return requestQueries.containsKey(queryID) || personalQueries.containsKey(queryID);
    }

    /**
     * Determines whether this peer has already handled a received response
     * 
     * @param queryID The ID for the response the peer needs to handle
     * @return True if the response has already been handled by this peer, false if
     *         the response has never been encounterd.
     */
    public static boolean seenResponse(String queryID) {
        return responseQueries.containsKey(queryID);
    }

    /**
     * Returns whether or not a received response is in response to a request this
     * peer sent.
     * 
     * @param queryID The string ID for the response for which query was responded
     *                to
     * @return True if this peer sent out the request, false otherwise
     */
    public static boolean responseForMe(String queryID) {
        return personalQueries.containsKey(queryID);
    }

    /**
     * Add a response to the seen responses
     * 
     * @param responseQuery The query to be added as already encountered by this
     *                      peer
     */
    public static void addResponse(Query responseQuery) {
        responseQueries.put(responseQuery.getID(), responseQuery);
    }

    /**
     * Add a request to the seen requests
     * 
     * @param request The query to be added as already encountered by this peer
     */
    public static void addRequest(Query request) {
        requestQueries.put(request.getFilename(), request);
    }

    /**
     * Adds a new handler to this peer to handle incoming connections.
     * 
     * @param incomingPeer The handler created by the welcome socket for a
     *                     connection TO this peer.
     */
    public static void addIncoming(RequestReceiver incomingPeer) {
        incomingPeers.put(incomingPeer.getIP(), incomingPeer);
    }

    /**
     * Removes an incoming connection when it is to be disconnected.
     * @param IP The string IP for the request receiver which is to be removed
     */
    public static void removeRequestReceiver(String IP) {
        incomingPeers.remove(IP);
    }

    /**
     * Removes a outgoing connection when leaving or disconnecting.
     * @param deadThread The handler to be removed
     */
    public static void removeRequestSender(RequestHandler deadThread) {
        peers.remove(deadThread);
    }

    /**
     * Hanndles the termination of incoming and outgoing connections from / to this peer.
     */
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

    /**
     * Handles retreiving a file from a different peer.
     * @param filename The filename that is to be requested
     * @param ip The IP for the peer which as the file
     * @param port The port number for the FileWelcome socket of the peer being requested from
     */
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