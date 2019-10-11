import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.io.IOException;

abstract class FileHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected boolean open;

    protected Timer heartbeatTimer = new Timer();

    public FileHandler (Socket socket){
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