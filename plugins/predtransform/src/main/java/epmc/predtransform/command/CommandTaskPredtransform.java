package epmc.predtransform.command;

import epmc.error.EPMCException;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;
import epmc.options.Options;

public class CommandTaskPredtransform implements CommandTask {
    public final static String IDENTIFIER = "predtransform";
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
    public ModelCheckerResult executeInServer() throws EPMCException {
    	System.out.println("HERE");
        // TODO
        // starting point for implementing the IC3 predicate generation
        // if you want to specify program options specific to this plugin,
        // please take a look at the existing plugins, e.g. the one for RDDL
        return null;
    }

}
