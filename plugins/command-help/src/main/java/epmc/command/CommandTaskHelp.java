package epmc.command;

import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;

public class CommandTaskHelp implements CommandTask {
    public final static String IDENTIFIER = "help";
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
    public void setModelChecker(ModelChecker modelChecker) {
    }

    @Override
	public void executeOnClient() {
        System.out.println(UsagePrinter.getUsage(options));
    }
    
    @Override
    public boolean isRunOnServer() {
        return false;
    }
}
