package epmc.command;

import epmc.jani.interaction.commandline.CommandLineCommand;
import epmc.jani.interaction.commandline.CommandLineOptions;
import epmc.jani.interaction.plugin.AfterCommandLineOptionsCreation;

public class AfterCommandLineOptionsCreationCheck implements AfterCommandLineOptionsCreation {
    private final static String CHECK = "check";
    
    @Override
    public String getIdentifier() {
        return CHECK;
    }

    @Override
    public void process(CommandLineOptions options) {
        assert options != null;
        options.addCommand(new CommandLineCommand.Builder()
                .setBundleName(OptionsCommandCheck.OPTIONS_COMMAND_CHECK)
                .setIdentifier("check")
                .build(),
                new CommandJANICheck.Builder());
    }

}
