import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class FileReceiver extends FileHandler {

    private String filename;

    public FileReceiver(Socket socket, String filename) throws IOException {
        super(socket);
        this.filename = filename;
    }

    public void terminate() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
        try {
            System.out.println("Trying to receive the file");
            OutputStream fileOutput = new FileOutputStream("obtained\\" + filename);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutput);


            System.out.println("Send the file name");
            // Pull in a file from the data input stream.
            dos.writeUTF(filename);

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = dis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();

            this.terminate();
            System.out.println("Completed receiving file");
        } catch (IOException e) {

        }

    }

    // public static void

    @Override
    public void sendQuery(Query nextQuery) {

    }
}