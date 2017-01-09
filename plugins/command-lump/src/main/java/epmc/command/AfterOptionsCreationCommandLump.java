package epmc.command;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCommandLump implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-command-lump";

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
        	.setBundleName(OptionsCommandLump.OPTIONS_COMMAND_LUMP)
        	.setIdentifier(CommandTaskLump.IDENTIFIER)
        	.setCommandLine()
        	.setGui()
        	.setWeb()
        	.build();
        
        commandTaskClasses.put(CommandTaskLump.IDENTIFIER, CommandTaskLump.class);
        OptionTypeEnum optionTypeOutput = new OptionTypeEnum(OutputType.class);
        options.addOption().setBundleName(OptionsCommandLump.OPTIONS_COMMAND_LUMP)
        	.setIdentifier(OptionsCommandLump.LUMP_OUTPUT_TYPE)
        	.setType(optionTypeOutput).setDefault(OutputType.STATISTICS)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(OptionsGraphsolver.GRAPHSOLVER_CATEGORY)
        	.build();
    }
}
