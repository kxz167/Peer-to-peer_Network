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

    /**
     * Creates a new file receiver built from the file handler given a socket and
     * filename
     * 
     * @param socket   The socket which the file receiver will use to receive the
     *                 file
     * @param filename The file name the handler will need to request from the peer
     */
    public FileReceiver(Socket socket, String filename) {
        super(socket);
        this.filename = filename;
    }

    /**
     * Override the run method to pull the file from the sender.
     */
    @Override
    public void run() {

        System.out.println(
                "Requesting file from: " + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort());

        // Specify file output
        OutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream("obtained/" + filename);
        } catch (FileNotFoundException e) {
            System.out.println("Can't find the output file destination");
        }

        if (fileOutput != null) {

            BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);

            try {
                dos.writeUTF(filename);
            } catch (IOException e) {
                System.out.println("Writing filename failed");
            }

            // Read in the file at 1024 byte intervals
            int bytePos = 0;
            byte[] buffer = new byte[1024];

            try {
                while ((bytePos = dis.read(buffer)) != -1) {
                    bufferedOutput.write(buffer, 0, bytePos);
                }
            } catch (IOException e) {
                System.out.println("Error reading in the file from sender");
            }

            // Cleanup
            try {
                bufferedOutput.flush();
                bufferedOutput.close();
            } catch (IOException e) {
                System.out.println("Errors cleaning up BufferedOutputStream");
            }

            System.out.println("Completed receiving file from: " + this.socket.getInetAddress().getHostAddress() + ":"
                    + this.socket.getPort());

            this.terminate();
        }
    }
}