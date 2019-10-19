import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.io.IOException;

public abstract class RequestHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected boolean open;

    protected Timer heartbeatTimer = new Timer();

    public RequestHandler (Socket socket) throws IOException{
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public void terminate()throws IOException{
        heartbeatTimer.cancel();
        dos.writeUTF("Close");
        socket.close();
    }

    public abstract void sendQuery(Query nextQuery);
}