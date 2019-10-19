import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class FileSender extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;


    
    public FileSender(Socket socket, DataInputStream dis, DataOutputStream dos){
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void terminate()throws IOException{
        socket.close();
    }

    @Override
    public void run(){
        //Push requested file into the data output stream.
    }
}