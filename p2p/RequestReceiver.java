import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class RequestReceiver extends RequestHandler {

    private long TIMEOUT = 60000L;
    private boolean open = true;

    private TimerTask heartbeat = new TimerTask(){
        
        @Override
        public void run() {
            //Close the socket
            try {
                socket.close();
            } catch (IOException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
    };


    public RequestReceiver(Socket socket) throws IOException{
        super(socket);
    }

    @Override
    public void sendQuery(Query nextQuery){

    }

    @Override
    public void run() {
        // Handles heartbeat receiving. Close if none received in ____
        heartbeatTimer.schedule(heartbeat, TIMEOUT);

        while (open){
            String input = "";
            System.out.println("Trying to read input");
            try{
                input = dis.readUTF();
            }
            catch(IOException e){
                e.printStackTrace();
            }

            // Scanner inputScanner = new Scanner(input);

            switch (input){
                case "Heartbeat":
                    System.out.println("Heartbeat received");
                    refreshTimer();
                    break;
                case "Close":
                    System.out.println("Peer was closed");
                    open = false;//Close the socket
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //TODO: handle exception
                        e.printStackTrace();
                    }
                    break;
                default:
                    parseInput(input);
                    break;
            }
        }

        // Gets input requests

        //
    }

    public void refreshTimer(){
        heartbeat.cancel();
        heartbeatTimer = new Timer();
        heartbeatTimer.schedule(heartbeat, TIMEOUT);
    }

    public void parseInput(String input){
        Scanner inputScanner = new Scanner(input);
        inputScanner.useDelimiter(":");

        String queryType = inputScanner.next();

        inputScanner.useDelimiter(";");

        if (queryType == "Q"){
            String queryID = inputScanner.next();
            String filename = inputScanner.next();
            
            Query receivedQuery = new Query(false, queryID, filename, socket.getInetAddress().getHostAddress());
            
            //Query received, send a response if have with my port and ip, 
            
            //else flood queries

        }
        else{
            String queryID = inputScanner.next();
            inputScanner.useDelimiter(":");

            String peerIP = inputScanner.next();
            inputScanner.useDelimiter(";");
            
            int peerPort = inputScanner.nextInt();
            String filename = inputScanner.next();
            
            Query receivedQuery = new Query(true, queryID, peerIP, peerPort, filename, this.socket.getInetAddress().getHostAddress());

            //Response received, is this response mine? 
            
            //if not, send to whoever sent the query before (IP, port attached);
        }
    }

}