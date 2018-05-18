/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.modelchecker;

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
     */
    default void executeInClientBeforeServer() {
    }

    /**
     * Whether the tasks shall run on a server.
     * In case the method returns {@code false}, only
     * {@link #executeInClientBeforeServer()} shall be executed and no server shall be
     * started if the tool is run from command line. If the method returns
     * {@code true}, at first {@link #executeInClientBeforeServer()} shall be executed,
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
    
    default void executeInClientAfterServer() {
    }
}
