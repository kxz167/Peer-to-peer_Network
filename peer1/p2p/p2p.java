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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;

class p2p {

    private static boolean running = true;
    private static List<Socket> neighborClientSockets = new ArrayList<>();
    private static List<Socket> neighborServerSockets = new ArrayList<>();
    // private static Thread welcomeHandler;

    private static RequestWelcomeHandler requestWelcomeHandler;
    private static FileWelcomeHandler fileWelcomeHandler;

    private static List<RequestHandler> peerRequestConnections = new ArrayList<>();
    // private static List<FileHandler>;

    public static void main(String[] args) throws IOException {
        System.out.println("Hello");

        initWelcomeSockets();
        startWelcomeSockets();

        // TimerTask heartbeat = new TimerTask(){
        
        //     @Override
        //     public void run() {
        //         //Close the socket
        //         System.out.println("Heartbeat");
        //     }
        // };
        // Timer heartbeatTimer = new Timer();
        // heartbeatTimer.schedule(heartbeat, 60*1000, 60*1000);

        
    

        // Get user input.
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        while (running) {

            System.out.println("waiting");
            parse(inputReader.readLine());
        }

        inputReader.close();
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

            Socket newNeighbor = new Socket(ip, port);

            DataInputStream dis = new DataInputStream(newNeighbor.getInputStream());
            DataOutputStream dos = new DataOutputStream(newNeighbor.getOutputStream());

            peerRequestConnections.add(new RequestSender(newNeighbor, dis, dos));
        }

        neighborScanner.close();
    }

    public static void parse(String input) throws IOException{
        switch (input) {
        case "connect":
            initNeighborConnections();
            System.out.println("Connecting");

            break;
        case "exit":
            System.out.println("Exiting");
            requestWelcomeHandler.terminate();
            fileWelcomeHandler.terminate();
            running = false;
            break;
        case "print":
            System.out.println("Peer neighbors: " + peerRequestConnections);
            System.out.println("Incoming connections: " + requestWelcomeHandler.getConnections());

        default:
            System.out.println("Unknown command");
        }
    }

}