
public class Query {

    private boolean isResponse = false;
    private String ID;
    private String filename;
    private String peerIP;
    private int peerPort;

    private String previousIP;

    /**
     * A constructor to create a query which is one the p2p has sent out
     * @param isResponse Whether this query represents a response
     * @param ID The ID for the query as a string
     * @param filename The filename that is being requested
     */
    public Query(boolean isResponse, String ID, String filename) {
        this.isResponse = isResponse;
        this.ID = ID;
        this.filename = filename;
    }

    /**
     * A constructor to create a query which represents on the p2p has received and will forward.
     * @param isResponse Whether or not this query is a response
     * @param ID The ID for the query as a string
     * @param filename The filename for the requested file
     * @param previousIP The IP of the peer which had sent the query
     */
    public Query(boolean isResponse, String ID, String filename, String previousIP) {
        this(isResponse, ID, filename);
        this.previousIP = previousIP;
    }

    /**
     * Create a query which represents a response to a peer.
     * @param isResponse Whether or not this query is a response
     * @param ID The ID for the query which was received
     * @param locationIP The ip of the peer which has the file that was requested
     * @param locationPort The port for the peer which has the file tht was requested
     * @param filename The filename which this query is in response to
     * @param previousIP The IP For where this response came from.
     */
    public Query(boolean isResponse, String ID, String locationIP, int locationPort, String filename, String previousIP) {
        this(isResponse, ID, filename, previousIP);
        this.peerIP = locationIP;
        this.peerPort = locationPort;
    }

    /**
     * Whether or not the query is a response or not
     * @return True if this query is a response query, false otherwise
     */
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