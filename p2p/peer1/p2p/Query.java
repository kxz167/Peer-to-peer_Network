
public class Query {

    private boolean isResponse = false;
    private String ID;
    private String filename;
    private String peerIP;
    private int peerPort;

    private String previousIP;

    //My query
    public Query(boolean isResponse, String ID, String filename) {
        this.isResponse = isResponse;
        this.ID = ID;
        this.filename = filename;
    }

    //Passed on queries
    public Query(boolean isResponse, String ID, String filename, String previousIP) {
        this(isResponse, ID, filename);
        this.previousIP = previousIP;
    }

    //Responses
    public Query(boolean isResponse, String ID, String locationIP, int locationPort, String filename, String previousIP) {
        this(isResponse, ID, filename, previousIP);
        this.peerIP = locationIP;
        this.peerPort = locationPort;
    }


    public boolean isResponse() {
        return this.isResponse;
    }

    public String getID() {
        return this.ID;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getPeerIP() {
        return this.peerIP;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public String getPreviousIP() {
        return this.previousIP;
    }

    public void setPeerIP(String peerIP){
        this.peerIP = peerIP;
    }

    public void setPeerPort(int peerPort){
        this.peerPort = peerPort;
    }

}