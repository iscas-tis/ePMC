package epmc.command;

import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;

public class CommandTaskCheck implements CommandTask {
    public final static String IDENTIFIER = "check";
    private ModelChecker modelChecker;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void executeInServer() {
    	modelChecker.check();
    }
}
