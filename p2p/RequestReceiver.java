import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

class RequestReceiver extends RequestHandler {

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

}