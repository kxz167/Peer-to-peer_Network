import java.io.DataInputStream;
import java.io.DataOutputStream;

class RequestHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    public RequestHandler (Socket socket, DataInputStream dis, DataOutputStream dos){
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void terminate()throws IOException{
        socket.close();
    }

}