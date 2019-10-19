import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;

class RequestHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected boolean open;

    public RequestHandler (Socket socket, DataInputStream dis, DataOutputStream dos){
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void terminate()throws IOException{
        socket.close();
    }

}