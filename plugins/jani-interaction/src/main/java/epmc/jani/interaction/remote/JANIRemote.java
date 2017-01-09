package epmc.jani.interaction.remote;

import java.rmi.RemoteException;

import epmc.modelchecker.RawModel;
import epmc.options.Options;

/**
 * RMI interface for EPMC server.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANIRemote extends java.rmi.Remote {
    /**
     * Execute task on EPMC server.
     * 
     * @param model unparsed model to check
     * @param options options used
     * @param channel channel to send information back to client
     * @return result of model checking process
     * @throws RemoteException thrown in case of connection problems
     */
    public void execute
    (RawModel model, Options options, EPMCChannel channel)
            throws RemoteException;
}
