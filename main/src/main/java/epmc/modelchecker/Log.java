package epmc.modelchecker;

import epmc.error.EPMCException;
import epmc.messages.Message;

// TODO complete documentation

public interface Log {
    
    void send(Message key, Object... params);

    public void send(EPMCException exception);

    public void send(ModelCheckerResult result);

    public void setSilent(boolean silent);
    
    public boolean isSilent();
}
