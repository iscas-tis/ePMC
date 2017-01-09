package epmc.modelchecker;

import epmc.error.EPMCException;
import epmc.options.Options;

/**
 * Task which should be performed by EPMC.
 * Tasks include e.g. model checking, showing available options, exploring the
 * model, etc. Some tasks should be run directly on the client side of the
 * tool when called from the command line, such as for instance a task
 * providing an overview of available options. Other tasks should be run on
 * a task server, because
 * <ul>
 * <li>they run for a long time and thus should not block the tool,</li>
 * <li>they might potentially crash the tool, which is bad in case EPMC
 * runs as a web server.</li>
 * </ul>
 * 
 * @author Ernst Moritz Hahn
 */
public interface CommandTask {
    /**
     * Get unique identifier of this task.
     * 
     * @return unique identifier of this task.
     */
    String getIdentifier();

    /**
     * Set options to use for this task.
     * This method shall be called before
     * {@link #executeOnClient()} is called. 
     * 
     * @param options options to use for this task
     */
    default void setOptions(Options options) {
    }
    
    /**
     * Set model checker to use for this task.
     * This method shall be called on the server before
     * {@link #executeInServer()} is called.
     * 
     * @param modelChecker model checker to use for this task.
     */
    default void setModelChecker(ModelChecker modelChecker) {
    }

    /**
     * Run part of the task to be executed on the client.
     * 
     * @throws EPMCException thrown in case of problems
     */
    default void executeOnClient() throws EPMCException {
    }

    /**
     * Whether the tasks shall run on a server.
     * In case the method returns {@code false}, only
     * {@link #executeOnClient()} shall be executed and no server shall be
     * started if the tool is run from command line. If the method returns
     * {@code true}, at first {@link #executeOnClient()} shall be executed,
     * then a server shall be started on which then
     * {@link CommandTask#executeInServer()} shall be executed.
     * 
     * @return whether the tasks shall run on a server
     */
    default boolean isRunOnServer() {
        return true;
    }

    /**
     * Run part of the task to be executed on the server.
     */
    default void executeInServer() {
    }
}
