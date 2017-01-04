package epmc.webserver.options;

import epmc.error.EPMCException;
import epmc.options.Command;
import epmc.options.Options;

public class CommandBackend implements Command {
    private final static String IDENTIFIER = "backend";

    private Options options;

	@Override
    public void setOptions(Options options) {
        assert options != null;
        this.options = options;
    }

    @Override
    public String getParameterDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getShortDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLongDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void parse(String... parameters) throws EPMCException {
        // TODO Auto-generated method stub
        
    }

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}


}
