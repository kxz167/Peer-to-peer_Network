import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

class RequestSender extends RequestHandler {

    public RequestSender(Socket socket) throws IOException{
        super(socket);
    }

    @Override
    public void run() {

        // Handles heartbeat sending
        TimerTask heartbeat = new TimerTask(){
        
            @Override
            public void run() {
                try{
                    
                    dos.writeUTF("Heartbeat");

                    System.out.println("Heartbeat sent");
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        };

        System.out.println("Heartbeat Created");

        Long heartbeatInterval = 10000L;
        heartbeatTimer.scheduleAtFixedRate(heartbeat, heartbeatInterval, heartbeatInterval);

        // Gets input requests

        //
    }

    @Override
    public void sendQuery(Query nextQuery){
        System.out.println("I am sending query");
    }
}