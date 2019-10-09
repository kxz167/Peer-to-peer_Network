import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class FileReceiver extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public FileReceiver(Socket socket, DataInputStream dis, DataOutputStream dos){
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void terminate() throws IOException{
        socket.close();
    }

    @Override
    public void run() {
        // Pull in a file from the data input stream.
    }
}