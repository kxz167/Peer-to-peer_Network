import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;

public abstract class RequestHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected boolean open = true;

    protected Timer heartbeatTimer = new Timer();
    protected TimerTask heartbeat = null;

    public RequestHandler (Socket socket) throws IOException{
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public void terminate()throws IOException{
        System.out.println("Termination of yes");
        open = false;
        
        heartbeat.cancel();
        heartbeatTimer.cancel();

        dos.writeUTF("Close");

        dis.close();
        dos.close();

        socket.close();
    }

    public String getIP(){
        return socket.getInetAddress().getHostAddress();
    }

    public abstract void sendQuery(Query nextQuery) throws IOException;
}