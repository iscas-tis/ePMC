package epmc.jani.interaction.remote;

import static epmc.error.UtilError.fail;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import epmc.error.EPMCException;
import epmc.options.Options;

/**
 * Task server which runs in the same process as the one who starts it.
 * Running the task server in the same process as the starting process means
 * that it cannot be easily stopped and might also crash the client. Thus, it
 * should only be used for testing, or for the command line, in which anyway no
 * further reaction is possible after a crash.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TaskServerSameProcess implements TaskServer {
    /** Options used for this task server. */
    private Options options;
    /** Remote server object for this task server. */
    private JANIServer server;

    @Override
    public void setOptions(Options options) {
        assert options != null;
        this.options = options;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void start() throws EPMCException {
        try {
            server = new JANIServer(null, 0);
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE, e);
        }
    }

    @Override
    public void stop() throws EPMCException {
        try {
            UnicastRemoteObject.unexportObject(server, true);
        } catch (NoSuchObjectException e) {
            // we don't care
        }
    }

    @Override
    public JANIRemote getServer() {
        assert server != null;
        return server;
    }
}
