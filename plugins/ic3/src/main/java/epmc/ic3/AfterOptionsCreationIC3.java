package epmc.ic3;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.ic3.command.CommandTaskCheckIC3;
import epmc.ic3.command.OptionsCommandIC3;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationIC3 implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-command-checkic3";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_TASK_CLASS);
        assert commandTaskClasses != null;
        options.addCommand(OptionsCommandIC3.OPTIONS_COMMAND_CHECK, OptionsCommandIC3.COMMAND_CHECK, true, true, true);
        commandTaskClasses.put(OptionsCommandIC3.COMMAND_CHECK, CommandTaskCheckIC3.class);
    }

}
