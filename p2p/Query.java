class Query {
    
    private boolean isResponse = false;
    private int ID;
    private String filename;
    private String peerIP;
    private int peerPort;

    public Query (boolean isResponse, int ID, String filename){
        this.isResponse = isResponse;
        this.ID = ID;
        this.filename = filename;
    }

    public Query (boolean isResponse, int ID, String filename, String peerIP, int peerPort){
        this(isResponse, ID, filename);
        this.peerIP = peerIP;
        this.peerPort = peerPort;
    }
}