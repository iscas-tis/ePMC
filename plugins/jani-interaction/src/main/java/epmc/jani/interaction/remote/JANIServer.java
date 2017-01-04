package epmc.jani.interaction.remote;

import static epmc.error.UtilError.ensure;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import epmc.error.EPMCException;
import epmc.main.Analyse;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.RawModel;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;

/**
 * JANI RMI server.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIServer extends UnicastRemoteObject implements JANIRemote {
    /** serial version ID for this class - 1L as I don't know any better */
    private static final long serialVersionUID = 1L;
    /** name of exit command */
    public final static String EXIT = "exit";

    /**
     * Construct new EPMC server
     * 
     * @param name name of server
     * @param port port on which to start server
     * @throws RemoteException thrown in case of communication failure
     */
    public JANIServer(String name, int port) throws RemoteException {
        super(port);
    }

    @Override
    public void execute(RawModel rawModel, Options options, EPMCChannel channel) throws RemoteException {
        try {
            String commandName = options.getString(Options.COMMAND);
            ensure(commandName != null, ProblemsRemote.REMOTE_NO_COMMAND_GIVEN);
            if (commandName.equals(EXIT)) {
                UnicastRemoteObject.unexportObject(this, true);
                return;
            }
            UtilOptionsEPMC.prepareOptions(options);
            ensure(channel != null, ProblemsRemote.REMOTE_CHANNEL_MISSING);
            UtilPlugin.loadPlugins(options);
            LogJANI log = new LogJANI(options, channel);
            options.set(OptionsMessages.LOG, log);
            Analyse.execute(rawModel, options, log);
        } catch (EPMCException e) {
            channel.send(e);
        }
    }

}
