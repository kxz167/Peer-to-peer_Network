import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

public class FileSender extends FileHandler {

    public FileSender(Socket socket) throws IOException {
        super(socket);
    }

    public void terminate() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
        // Push requested file into the data output stream.
        try {
            File file = new File("shared\\" + dis.readUTF());
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] buffer;
            long fileLength = file.length();
            long currentPos = 0;

            System.out.println("Commence file sending");

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

            System.out.println("Completed sending file");

            dos.flush();
            this.terminate();
        } catch (IOException e) {
            System.out.println("Ioexception");
        }
    }

    @Override
    public void sendQuery(Query nextQuery) {

    }
}