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

package epmc.jani.interaction.remote;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class containing problem descriptions in connection with the task server.
 * The class is protected from instantiation, because it only contains static
 * fields and method.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsRemote {
    /** Base name of file containing problem descriptors. */
    public final static String PROBLEMS_REMOTE = "ProblemsRemote";
    /** General problem with remote server. */
    public final static Problem REMOTE = newProblem("remote");
    /** We were not able to obtain an unused random port. */
    public final static Problem REMOTE_RANDOM_PORT_NOT_FOUND = newProblem("remote-random-port-not-found");
    /** It was not possible to create the RMI registry. */
    public final static Problem REMOTE_CREATE_REGISTRY_FAILED = newProblem("remote-create-registry-failed");
    /** An I/O exception occurred while trying to create a new process. */
    public final static Problem REMOTE_PROCESS_CREATE_IO_EXCEPTION = newProblem("remote-process-create-io-exception");
    /** An I/O exception occurred while trying to read the {@code stdout} of the new process. */
    public final static Problem REMOTE_PROCESS_READ_OUTPUT_IO_EXCEPTION = newProblem("remote-process-read-output-io-exception");
    /** The output line read from the server was {@code null}. */
    public static final Problem REMOTE_PROCESS_OUTPUT_NULL = newProblem("remote-process-output-null");
    /** The output the server returned by {@code stdout} was incorrect. */
    public static final Problem REMOTE_OUTPUT_INCORRECT = newProblem("remote-process-output-incorrect");
    /** It was not possible to obtain the registry. */
    public static final Problem REMOTE_GET_REGISTRY_FAILED = newProblem("remote-get-registry-failed");
    /** A remote exception occurred while trying to lookup the registry. */
    public static final Problem REMOTE_REGISTRY_LOOKUP_REMOTE_EXCEPTION = newProblem("remote-registry-lookup-remote-exception");
    /** A not-bound exception occurred while trying to lookup the registry. */
    public static final Problem REMOTE_NOT_BOUND_EXCEPTION = newProblem("remote-not-bound-exception");
    /** It was not possible to terminate the server process by any means. */
    public static final Problem REMOTE_FAILED_TERMINATE_PROCESS = newProblem("remote-failed-terminate-process");
    /** A remote exception occurred while trying to execute a task on a task server. */
    public static final Problem REMOTE_EXECUTE_REMOTE_EXCEPTION = newProblem("remote-execute-remote-exception");
    /** The remote channel given was {@code null} while trying to execute a task on the task server. */
    public final static Problem REMOTE_CHANNEL_MISSING = newProblem("remote-channel-missing");
    /** When trying to execute a task on the task server, no command to execute was given. */
    public final static Problem REMOTE_NO_COMMAND_GIVEN = newProblem("remote-no-command-given");

    /**
     * Construct new problem description from given name.
     * The base name of the properties file used is {@link #PROBLEMS_REMOTE}.
     * The paramter of the method must not be {@code null}.
     * 
     * @param name identifier of problem description constructed
     * @return problem description
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_REMOTE, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsRemote() {
    }
}
