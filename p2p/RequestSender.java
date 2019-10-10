import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class RequestSender extends RequestHandler{

    public RequestSender(Socket socket, DataInputStream dis, DataOutputStream dos){
        super(socket, dis, dos);
    }

    @Override
    public void run(){
        // Handles heartbeat sending

        // Gets input requests

        //  
    }
}