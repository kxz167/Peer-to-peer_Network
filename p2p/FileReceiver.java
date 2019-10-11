import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class FileReceiver extends FileHandler {

    public FileReceiver(Socket socket)throws IOException{
        super(socket);
    }

    public void terminate() throws IOException{
        socket.close();
    }

    @Override
    public void run() {
        // Pull in a file from the data input stream.
    }

    @Override
    public void sendQuery(Query nextQuery){

    }
}