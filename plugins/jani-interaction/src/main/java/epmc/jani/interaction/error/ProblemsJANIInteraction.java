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

package epmc.jani.interaction.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in JANI interaction plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIInteraction {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String ERROR_JANI_INTERACTION = "ErrorJANIInteraction";
    /** Client is not logged in. */
    public final static Problem JANI_INTERACTION_NOT_LOGGED_IN = newProblem("jani-interaction-not-logged-in");
    /** Client was already logged in before. */
    public final static Problem JANI_INTERACTION_ALREADY_LOGGED_IN = newProblem("jani-interaction-already-logged-in");
    /** Login data was provided in standard I/O mode. */
    public final static Problem JANI_INTERACTION_STD_IO_LOGIN_PROVIDED = newProblem("jani-interaction-std-io-login-provided");
    /** Message format is invalid. */
    public final static Problem JANI_INTERACTION_INVALID_MESSAGE = newProblem("jani-interaction-invalid-message");
    /** Login data was not provided in WebSocket mode. */
    public final static Problem JANI_INTERACTION_WEBSOCKET_NO_LOGIN = newProblem("jani-interaction-websocket-no-login");
    /** No password was provided. */
    public final static Problem JANI_INTERACTION_NO_PASSWORD = newProblem("jani-interaction-no-password");
    /** No password was provided. */
    public final static Problem JANI_INTERACTION_NO_ACCORDING_VERSION_FOUND = newProblem("jani-interaction-no-according-version-found");
    /** Login data is invalid */
    public final static Problem JANI_INTERACTION_LOGIN_DATA = newProblem("jani-interaction-login-data");
    /** Option is invalid. */
    public final static Problem JANI_INTERACTION_INVALID_OPTION = newProblem("jani-interaction-invalid-option");
    /** User has a running analysis with the same ID. */
    public final static Problem JANI_INTERACTION_ANALYSIS_SAME_ID = newProblem("jani-interaction-analysis-same-id");
    /** User tries to change an option it is not allowed to change. */
    public final static Problem JANI_INTERACTION_CHANGE_OPTION_FORBIDDEN = newProblem("jani-interaction-change-option-forbidden");
    /** Failed to bind JANI WebSockets server to socket. */
    public final static Problem JANI_INTERACTION_SERVER_BIND_FAILED = newProblem("jani-interaction-server-bind-failed");
    /** General I/O error when trying to bind JANI WebSocket server to socket. */
    public final static Problem JANI_INTERACTION_SERVER_IO_PROBLEM = newProblem("jani-interaction-server-io-problem");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(ERROR_JANI_INTERACTION, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIInteraction() {
    }
}
