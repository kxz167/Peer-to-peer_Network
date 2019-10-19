import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

class RequestSender extends RequestHandler {

    public RequestSender(Socket socket, DataInputStream dis, DataOutputStream dos) {
        super(socket, dis, dos);
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

        Timer heartbeatTimer = new Timer();
        Long heartbeatInterval = Duration.ofSeconds(30).toMinutes();
        heartbeatTimer.scheduleAtFixedRate(heartbeat, heartbeatInterval, heartbeatInterval);

        // Gets input requests

        //
    }
}