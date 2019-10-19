import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

class RequestReceiver extends RequestHandler {

    private int TIMEOUT = 60000;
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

    private Timer heartbeatTimer = new Timer();


    public RequestReceiver(Socket socket, DataInputStream dis, DataOutputStream dos) {
        super(socket, dis, dos);
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

            switch (input){
                case "Heartbeat":
                    System.out.println("Heartbeat received");
                    refreshTimer();
                    break;
                    case "Close":
                        System.out.println("Peer was closed");
                        open = false;
            }
        }

        // Gets input requests

        //
    }

    public void refreshTimer(){
        heartbeatTimer.cancel();
        heartbeatTimer = new Timer();

        TimerTask newHeartbeat = new TimerTask(){
        
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

        heartbeatTimer.schedule(newHeartbeat, TIMEOUT);
    }

}