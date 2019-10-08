import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

class p2p {

    private static boolean running = true;
    private static List<Socket> openSockets = new List<>();

    public static void main (String[] args) throws IOException{
        System.out.println("Hello");

        //Get user input.
        BufferedReader inputReader = new BufferedReader(new InputStreamReader (System.in));

        while(running){
            parse(inputReader.readLine());
        }

        inputReader.close();

        //Create one TCP port for file transfers
    
        //Wait for command line input;
    }

    public static void initiateNeighborConnections(){
        //Create one TCP port for neighbor connections (from config_neighbors.txt)
            //Read from config_neighbors.txt the ip address and port numbers
        File config_neighbors = new File("config_neighbors.txt");

        Scanner neighborScanner = new Scanner (config_neighbors);

        while (neighborScanner.hasNext()){
            String ip = neighborScanner.next();
            int port = neighborScanner.nextInt();

            openSockets.add(new Socket(ip, port));
        }
        
    }

    public static void parse(String input){
        switch(input){
            case "connect":
                System.out.println("Connecting");
                break;
            case "exit":
                System.out.println("Exiting");
                running = false;
                break;
        }
    }


    
}