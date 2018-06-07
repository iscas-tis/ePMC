package epmc.command;

import epmc.jani.interaction.commandline.CommandLineCommand;
import epmc.jani.interaction.commandline.CommandLineOptions;
import epmc.jani.interaction.plugin.AfterCommandLineOptionsCreation;

public class AfterCommandLineOptionsCreationHelp implements AfterCommandLineOptionsCreation {
    private final static String HELP = "help";
    
    @Override
    public String getIdentifier() {
        return HELP;
    }

    @Override
    public void process(CommandLineOptions options) {
        assert options != null;
        options.addCommand(new CommandLineCommand.Builder()
                .setBundleName(OptionsCommandHelp.OPTIONS_COMMAND_HELP)
                .setIdentifier("help")
                .build(),
                new CommandJANIHelp.Builder());
    }

}
