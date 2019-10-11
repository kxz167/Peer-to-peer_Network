import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class FileSender extends FileHandler {

    public FileSender(Socket socket)throws IOException{
        super(socket);
    }

    public void terminate()throws IOException{
        socket.close();
    }

    @Override
    public void run(){
        //Push requested file into the data output stream.
    }

    @Override
    public void sendQuery(Query nextQuery){

    }
}