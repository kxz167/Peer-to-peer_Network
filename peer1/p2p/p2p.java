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
import java.time.Duration;

public class p2p {

    private static String myHost;

    private static List<String> myFiles = new ArrayList<>();

    private static boolean running = true;
    // private static List<Socket> neighborClientSockets = new ArrayList<>();
    // private static List<Socket> neighborServerSockets = new ArrayList<>();
    // private static Thread welcomeHandler;

    private static RequestWelcomeHandler requestWelcomeHandler;
    private static FileWelcomeHandler fileWelcomeHandler;

    private static List<RequestHandler> peers = new ArrayList<>();
    private static Map<String, RequestHandler> incomingPeers = new TreeMap<>();

    // Something to hold queries
    private static Map<String, Query> requestQueries = new TreeMap<>();
    private static Map<String, Query> personalQueries = new TreeMap<>();
    private static Map<String, Query> responseQueries = new TreeMap<>();
    private static Map<String, Query> personalResponses = new TreeMap<>();

    public static void main(String[] args) throws IOException, UnknownHostException {

        // Set frequently accessed host name
        myHost = InetAddress.getLocalHost().getHostAddress();

        System.out.println("Starting host: " + myHost);

        // Initiation
        initWelcomeSockets();
        startWelcomeSockets();
        findMyFiles();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        // Take commands
        while (running) {
            parseInput(inputReader.readLine());
        }

        inputReader.close();
    }

    // INITIATION:
    public static void initWelcomeSockets() throws FileNotFoundException {

        File config_neighbors = new File("config_peer.txt");
        Scanner portScanner = new Scanner(config_neighbors);

        requestWelcomeHandler = new RequestWelcomeHandler(portScanner.nextInt());
        fileWelcomeHandler = new FileWelcomeHandler(portScanner.nextInt());

        portScanner.close();
    }

    public static void startWelcomeSockets() {
        requestWelcomeHandler.start();
        fileWelcomeHandler.start();
    }

    public static void initNeighborConnections() throws FileNotFoundException, UnknownHostException, IOException {

        File config_neighbors = new File("config_neighbors.txt");
        Scanner neighborScanner = new Scanner(config_neighbors);

        while (neighborScanner.hasNext()) {
            String ip = neighborScanner.next();
            int port = neighborScanner.nextInt();

            System.out.print("Attempting to connect to peer: " + ip + ":" + port + "... ");

            try {
                RequestHandler connection = new RequestSender(new Socket(ip, port));
                System.out.println("Success");
                peers.add(connection);
            } catch (ConnectException e) {
                // Can't connect
                System.out.println("Failure");
            }
        }

        neighborScanner.close();
    }

    public static void startNeighborConnections() {
        for (RequestHandler handler : peers) {
            handler.start();
        }
    }

    public static void findMyFiles() throws FileNotFoundException {

        File config_sharing = new File("config_sharing.txt");
        Scanner fileScanner = new Scanner(config_sharing);

        while (fileScanner.hasNext()) {
            myFiles.add(fileScanner.next());
        }
    }

    // QUERIES
    public static void floodPeers(Query nextQuery) throws IOException {

        for (RequestHandler connection : peers) {
            connection.sendQuery(nextQuery);
        }
        System.out.println("Sending query: " + nextQuery.getID());

    }

    // REQUEST QUERIES:
    public static void createQuery(String filename) throws IOException {

        String queryID = myHost + filename;
        Query nextQuery = new Query(false, queryID, filename);

        personalQueries.put(queryID, nextQuery);
        floodPeers(nextQuery);
    }

    public static void forwardRequestQuery(Query nextQuery) throws IOException {
        requestQueries.put(nextQuery.getID(), nextQuery);

        floodPeers(nextQuery);
    }

    // RESPONSE QUERIES:
    public static void forwardResponseQuery(Query response) throws IOException {
        String destinationIP = requestQueries.get(response.getID()).getPreviousIP();
        
        incomingPeers.get(destinationIP).sendQuery(response);
    }

    // GETTERS
    public static Map<String, Query> getPersonalResponses() {
        return personalResponses;
    }

    public static String getFileSocketIP() {
        return fileWelcomeHandler.getIP();
    }

    public static int getFileSocketPort() {
        return fileWelcomeHandler.getPort();
    }

    public static Map<String, Query> getRequestQueries() {
        return requestQueries;
    }

    public static Map<String, Query> getResponseQueries() {
        return responseQueries;
    }

    // INFO GETTERS
    public static boolean hasFile(String filename) {
        return myFiles.contains(filename);
    }

    public static boolean seenRequest(String queryID){
        return requestQueries.containsKey(queryID);
    }

    public static boolean seenResponse(String queryID){
        return responseQueries.containsKey(queryID);
    }

    public static boolean myResponse(String queryID) {
        return personalQueries.containsKey(queryID);
    }

    //ADDERS
    public static void addPersonalResponse(Query responseQuery){
        
        String queryID = responseQuery.getID();

        personalResponses.put(queryID, responseQuery);
        responseQueries.put(queryID, responseQuery);

    }

    public static void addResponseQuery(Query responseQuery){
        responseQueries.put(responseQuery.getID(), responseQuery);
    }

    // SETTERS
    public static void addIncoming(RequestReceiver incomingPeer) {
        incomingPeers.put(incomingPeer.getIP(), incomingPeer);
    }


    public static void addRequest(Query request) {
        requestQueries.put(request.getFilename(), request);
    }

    public static boolean hasRequest(Query request) {
        return requestQueries.containsKey(request.getFilename());
    }


    // FILE RETREIVAL
    public static void retreiveFile(String filename, String ip, int port) {
        try {

            Socket fileSocket = new Socket(ip, port);

            FileReceiver fileReceiver = new FileReceiver(fileSocket, filename);
            fileReceiver.start();

        } catch (UnknownHostException e) {
            System.out.println("Unknown Host");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    // P2P FUNCTIONALITY
    public static void parseInput(String input) throws IOException {
        Scanner inputScanner = new Scanner(input);
        String command = inputScanner.next();
        command = command.toLowerCase();

        switch (command) {
        case "connect":
            initNeighborConnections();
            startNeighborConnections();
            break;
        case "exit":
            requestWelcomeHandler.terminate();
            fileWelcomeHandler.terminate();
            for (RequestHandler connection : peers) {
                connection.terminate();
            }
            running = false;
            break;
        case "get":
            createQuery(inputScanner.next());
            break;
        default:
            System.out.println("Unknown command, please try again");
        }
    }

}