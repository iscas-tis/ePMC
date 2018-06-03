package epmc.jani.interaction.command;

import epmc.jani.interaction.communication.BackendFeedback;
import epmc.jani.interaction.communication.BackendInterface;
import epmc.modelchecker.CommandTask;

public interface CommandTaskJANIClient extends CommandTask, BackendFeedback {
    void setBackend(Object client, BackendInterface backend);
}
