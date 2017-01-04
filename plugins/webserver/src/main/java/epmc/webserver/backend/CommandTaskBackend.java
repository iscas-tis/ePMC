package epmc.webserver.backend;

import epmc.error.EPMCException;
import epmc.modelchecker.CommandTask;
import epmc.webserver.options.OptionsWebserver;
import epmc.options.Options;

public class CommandTaskBackend implements CommandTask {
    private final static String IDENTIFIER = "backend";
    private Options options;
    
    @Override
    public void setOptions(Options options) {
    	assert this.options == null;
    	assert options != null;
    	this.options = options;
    }
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void executeOnClient() throws EPMCException {
        String backendProperties = options.getString(OptionsWebserver.BACKEND_PROPERTIES);
        String[] args = new String[1];
        args[0] = backendProperties;
        BackendEngine.main(args);
    }
    
    @Override
    public boolean isRunOnServer() {
    	return false;
    }
}
