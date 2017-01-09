package epmc.command;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCommandCheck implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-command-check";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;
        options.addCommand()
        	.setBundleName(OptionsCommandCheck.OPTIONS_COMMAND_CHECK)
        	.setIdentifier(CommandTaskCheck.IDENTIFIER)
        	.setCommandLine()
        	.setGui()
        	.setWeb().build();
        commandTaskClasses.put(OptionsCommandCheck.CHECK.name().toLowerCase(), CommandTaskCheck.class);
    }
}
