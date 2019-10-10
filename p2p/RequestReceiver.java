import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class RequestReceiver extends RequestHandler{

    

    public RequestReceiver(Socket socket, DataInputStream dis, DataOutputStream dos){
        super (socket, dis, dos);
    }

    @Override
    public void run(){
        // Handles heartbeat receiving. Close if none received in ____

        // Gets input requests

        //  
    }

    
}