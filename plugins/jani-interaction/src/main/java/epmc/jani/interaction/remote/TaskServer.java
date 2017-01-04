package epmc.jani.interaction.remote;

import static epmc.error.UtilError.fail;

import java.rmi.RemoteException;

import epmc.error.EPMCException;
import epmc.modelchecker.RawModel;
import epmc.options.Options;

/**
 * Interface to store several types of server connections.
 * Classes implementing this interface could for instance be processes running
 * on the local machine as well as processes running remotely.
 * The servers this class represents are used to perform one or more analysis
 * for a given client. The servers represented by this class are under the
 * under the control of the client. In particular, the client can start and
 * stop such servers. Thus, this class is <emph>not</emph> meant to represent
 * e.g. webservers which should stay up and be used by several users.
 * It is useful to run analysis processes in separate processes as supported by
 * this class rather than in threads e.g. because
 * <ul>
 * <li>they can be safely terminated before finishing, and</li>
 * <li>they can be run on remote computers.</li>
 * </ul>
 *
 * @author Ernst Moritz Hahn
 */
public interface TaskServer {
    /**
     * Set options to use for this task server.
     * The options are are not the which will become the option set used for the
     * task itself (e.g. which model checking engine to use) but they rather
     * control details about how the task is started, e.g. command line options
     * of the Java virtual machine to use. The options may not be {@code null}.
     * This method may only be called once.
     * 
     * @param options options to use for this task server
     */
    void setOptions(Options options);

    /**
     * Obtain options used for this server process.
     * 
     * @return options used for this server process.
     */
    Options getOptions();

    /**
     * Start given server process.
     * The method may only be executed once {@link #setOptions(Options)} has
     * been called. It may not be called several times, not even if
     * {@link #stop} was called afterwards. Instead, a new task server object
     * should be created to restart the server.
     * 
     * @throws EPMCException thrown if problems happen during the start
     */
    void start() throws EPMCException;
    
    /**
     * Terminate given server process.
     * The method may only be called after {@link #start()} has been called.
     * It may only be called once for a given task server object.
     * 
     * @throws EPMCException thrown in case of problems terminating server
     */
	void stop() throws EPMCException;
	
	/**
	 * Obtain interface to execute tasks on server.
	 * This method may only be called once {@link #start()} has been called. It
	 * may not be called after {@link #stop()} has been called.
	 * 
	 * @return interface to execute tasks on server.
	 */
	JANIRemote getServer();
	
	/**
	 * Execute task on server
     * This method may only be called once {@link #start()} has been called. It
     * may not be called after {@link #stop()} has been called.
	 * 
	 * @param userOptions options to send to the server
	 * @param channel message channel to use
	 * @param model model to analyse
	 * @param ignoreConnectionErrors whether to silently ignore connection problems
	 * @throws EPMCException thrown in case of problems during analysis
	 */
    default void execute(Options userOptions, EPMCChannel channel, RawModel model, boolean ignoreConnectionErrors) throws EPMCException {
    	assert userOptions != null;
    	assert channel != null;
        assert model != null;
        userOptions = userOptions.clone();
        try {
            getServer().execute(model, userOptions, channel);
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE_EXECUTE_REMOTE_EXCEPTION, e.getMessage(), e);
        }
    }
}
