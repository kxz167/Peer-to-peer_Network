import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class FileReceiver extends FileHandler {

    private String filename;

    public FileReceiver(Socket socket, String filename){
        super(socket);
        this.filename = filename;
    }

    @Override
    public void run() {

        System.out.println(
                "Requesting file from: " + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort());

        OutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream("obtained/" + filename);
        } catch (FileNotFoundException e) {
            System.out.println("Can't find the output file destination");
        }

        if (fileOutput != null) {

            BufferedOutputStream bos = new BufferedOutputStream(fileOutput);

            try {
                dos.writeUTF(filename);
            } catch (IOException e) {
                System.out.println("Writing filename failed");
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            try {
                while ((bytesRead = dis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                System.out.println("Error reading in the file from sender");
            }

            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                System.out.println("Errors cleaning up BufferedOutputStream");
            }

            System.out.println("Completed receiving file from: " + this.socket.getInetAddress().getHostAddress() + ":"
                    + this.socket.getPort());

            this.terminate();
        }
    }
}