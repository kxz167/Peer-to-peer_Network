import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class RequestReceiver extends Thread{

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public RequestReceiver(Socket socket, DataInputStream dis, DataOutputStream dos){
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void terminate() throws IOException{
        socket.close();
    }

    @Override
    public void run(){
        // Handles heartbeat receiving. Close if none received in ____

        // Gets input requests

        //  
    }

    
}