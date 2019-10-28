import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.io.IOException;

public abstract class FileHandler extends Thread{
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected boolean open;

    // protected Timer heartbeatTimer = new Timer();

    public FileHandler (Socket socket){
        try{
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e){
            System.out.println("Error creating the socket for the file handler");
        }
    }

    public void terminate(){
        try{
            dis.close();
            dos.close();
            socket.close();
        }
        catch (IOException e){
            System.out.println("Sockets closed");
        }
    }
}