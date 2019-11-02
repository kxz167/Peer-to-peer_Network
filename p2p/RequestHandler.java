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

    /**
     * Creates a new request handler (for requests and responses) from one peer to another
     * @param socket The socket that the connection takes place on
     */
    public RequestHandler (Socket socket){
        try{
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            System.out.println("Socket closed");
        }
    }

    /**
     * Terminates the connection for the handler by stopping any kind of heartbeats and streams.
     */
    public void terminate(){
        try{

            open = false;
            
            heartbeat.cancel();
            heartbeatTimer.cancel();
    
            dos.writeUTF("Close");
            
            dis.close();
            dos.close();
    
            socket.close();

        }
        catch(IOException e){
            //Silent termination of the sockets. Good place for loggers
        }
    }

    public String getIP(){
        return socket.getInetAddress().getHostAddress();
    }

    public abstract void sendQuery(Query nextQuery);
}