import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

public class FileSender extends FileHandler {

    /**
     * Creates the file sender which will handle sending out the file when a peer
     * requests one
     * 
     * @param socket The socket which is connected to the requesting peer.
     */
    public FileSender(Socket socket) {
        super(socket);
    }

    /**
     * Override the run method so that file sending can happen independent of the
     * main p2p program.
     */
    @Override
    public void run() {
        String destination = this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
        // Push requested file into the data output stream.
        try {
            // Look for the file to be sent
            String filename = dis.readUTF();
            File file = new File("shared/" + filename);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] buffer;
            long fileLength = file.length();
            long currentPos = 0;

            System.out.println(filename + " requested by: " + destination);

            // push the file into the socket 1024 bytes at a time.
            while (currentPos != fileLength) {
                int pushSize = 1024;
                if (fileLength - currentPos >= pushSize) {
                    currentPos += pushSize;
                } else {
                    pushSize = (int) (fileLength - currentPos);
                    currentPos = fileLength;
                }

                buffer = new byte[pushSize];
                bis.read(buffer, 0, pushSize);
                dos.write(buffer);
            }

            System.out.println("Completed sending file to: " + destination);

            //Cleanup
            dos.flush();
            bis.close();

            this.terminate();

        } catch (IOException e) {
            System.out.println("Failed sending file to: " + destination);
        }
    }
}