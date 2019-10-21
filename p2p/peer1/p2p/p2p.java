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
    // private static List<FileHandler>;

    // Something to hold
    private static Map<String, Query> requestQueries = new TreeMap<>();
    // private static Map<String, Query> responseQueries = new TreeMap<>();
    private static Map<String, Query> personalQueries = new TreeMap<>();
    private static Map<String, Query> personalResponses = new TreeMap<>();
    private static Map<String, Query> responseQueries = new TreeMap<>();

    public static void main(String[] args) throws IOException, UnknownHostException {
        System.out.println("Hello");
        myHost = InetAddress.getLocalHost().getHostAddress();
        System.out.println(myHost);
        initWelcomeSockets();
        startWelcomeSockets();

        findMyFiles();

        // TimerTask heartbeat = new TimerTask(){

        // @Override
        // public void run() {
        // //Close the socket
        // System.out.println("Heartbeat");
        // }
        // };
        // Timer heartbeatTimer = new Timer();
        // heartbeatTimer.schedule(heartbeat, 60*1000, 60*1000);

        // Get user input.
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        while (running) {

            System.out.println("waiting");

            parseInput(inputReader.readLine());
        }

        inputReader.close();
    }

    public static void addIncoming(RequestReceiver incomingPeer) {
        incomingPeers.put(incomingPeer.getIP(), incomingPeer);
    }

    public static void findMyFiles() throws FileNotFoundException {
        File config_sharing = new File("config_sharing.txt");

        Scanner fileScanner = new Scanner(config_sharing);

        while (fileScanner.hasNext()) {
            myFiles.add(fileScanner.next());
        }
    }

    public static void createQuery(String filename) throws IOException {
        String queryID = myHost + filename;

        Query nextQuery = new Query(false, queryID, filename);

        personalQueries.put(queryID, nextQuery);

        floodPeers(nextQuery);
    }

    public static Map<String, Query> getPersonalResponses() {
        return personalResponses;
    }

    public static void forwardResponseQuery(Query response) throws IOException {
        String destinationIP = requestQueries.get(response.getID()).getPreviousIP();

        incomingPeers.get(destinationIP).sendQuery(response);
    }

    public static void forwardRequestQuery(Query nextQuery) throws IOException {
        requestQueries.put(nextQuery.getID(), nextQuery);
        floodPeers(nextQuery);
    }

    public static String getFileSocketIP() {
        return fileWelcomeHandler.getIP();
    }

    public static int getFileSocketPort() {
        return fileWelcomeHandler.getPort();
    }

    public static boolean hasFile(String filename) {
        return myFiles.contains(filename);
    }

    public static Map<String, Query> getRequestQueries() {
        return requestQueries;
    }

    public static Map<String, Query> getResponseQueries() {
        return responseQueries;
    }

    public static void floodPeers(Query nextQuery) throws IOException {

        for (RequestHandler connection : peers) {
            connection.sendQuery(nextQuery);
        }
        System.out.println("Sent query: " + nextQuery.getID());

    }

    public static void addRequest(Query request) {
        requestQueries.put(request.getFilename(), request);
    }

    public static boolean hasRequest(Query request) {
        return requestQueries.containsKey(request.getFilename());
    }

    public static boolean isMine(Query query) {
        return personalQueries.containsKey(query.getID());
    }

    // public static void addResponse(Query response){
    // responseQueries.add(response);
    // }

    // public static boolean hasResponse(Query response){
    // return responseQueries.contains(response);
    // }

    public static void retreiveFile(String filename, String ip, int port) {
        System.out.println("I will now try and retreive the file");
        try {
            System.out.println(ip);
            Socket fileSocket = new Socket(ip, port);

            FileReceiver fileReceiver = new FileReceiver(fileSocket, filename);
            fileReceiver.start();


        } catch (UnknownHostException e) {
            System.out.println("unknown Host");
        } catch (IOException e) {
            System.out.println("file errors");
        }

    }

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
        // Create one TCP port for neighbor connections (from config_neighbors.txt)
        // Read from config_neighbors.txt the ip address and port numbers
        File config_neighbors = new File("config_neighbors.txt");

        Scanner neighborScanner = new Scanner(config_neighbors);

        while (neighborScanner.hasNext()) {

            String ip = neighborScanner.next();
            int port = neighborScanner.nextInt();

            System.out.println(ip);
            System.out.println(port);
            try {
                RequestHandler connection = new RequestSender(new Socket(ip, port));
                peers.add(connection);
            } catch (ConnectException e) {
                // Can't connect
                System.out.println("Peer refused connection");
            }
        }

        neighborScanner.close();
    }

    public static void startNeighborConnections() {
        for (RequestHandler handler : peers) {
            handler.start();
        }
    }

    public static void parseInput(String input) throws IOException {
        Scanner inputScanner = new Scanner(input);

        switch (inputScanner.next()) {
        case "Connect":
            System.out.println("Connecting");
            initNeighborConnections();
            startNeighborConnections();
            break;
        case "Exit":
            System.out.println("Exiting");
            requestWelcomeHandler.terminate();
            fileWelcomeHandler.terminate();
            for (RequestHandler connection : peers) {
                connection.terminate();
            }
            running = false;
            break;
        case "Print":
            System.out.println("Peer neighbors: " + peers);
            break;
        case "Get":
            System.out.println("Will try and send out queries");
            createQuery(inputScanner.next());
            break;
        default:
            System.out.println("Unknown command");
        }
    }

}