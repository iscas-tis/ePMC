package epmc.jani.interaction.remote;

/**
 * Stores the name and port of an RMI server.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RMIConnectionData {
    /** name of RMI server */
    private final String name;
    /** port of RMI server */
    private final int port;

    /**
     * Constructs new data set of RMI connection data.
     * The server name must not be {@code null}. The port must be a positive
     * integer. Value 0 is not allowed, because the port must have indeed been
     * fixed at this point.
     * 
     * @param name name to store
     * @param port port to store
     */
    RMIConnectionData(String name, int port) {
        assert name != null;
        assert port > 0;
        this.name = name;
        this.port = port;
    }
    
    /**
     * Obtain server name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtain port number.
     * 
     * @return port number
     */
    public int getPort() {
        return port;
    }
}
