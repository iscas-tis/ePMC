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

package epmc.messages;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsMessages {
    private final static String PROBLEMS_MESSAGES = "ProblemsMessages";
    public final static Problem REMOTE = newProblem("remote");
    public final static Problem CHANNEL_FAILURE = newProblem("channel-failure");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(PROBLEMS_MESSAGES, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsMessages() {
    }
}
