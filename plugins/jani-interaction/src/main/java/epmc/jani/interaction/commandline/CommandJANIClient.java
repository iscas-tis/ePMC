package epmc.jani.interaction.commandline;

import epmc.jani.interaction.communication.BackendFeedback;
import epmc.jani.interaction.communication.BackendInterface;

public interface CommandJANIClient extends BackendFeedback {
    interface Builder {
        Builder setClient(Object client);
        
        Builder setBackend(BackendInterface backend);

        Builder setOptions(CommandLineOptions options);

        Builder setArgs(String[] args);
        
        CommandJANIClient build();
    }
}
