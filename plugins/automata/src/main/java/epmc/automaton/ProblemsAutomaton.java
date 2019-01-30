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

package epmc.automaton;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in automaton part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsAutomaton {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_AUTOMATON = "ProblemsAutomaton";

    /**
     * Insufficiently many automaton states for explicit/symbolic product.
     * This problem occurs when a product of an explicit-state automaton with
     * a DD-symbolic model shall be constructed but the number of states of the
     * automaton is larger than the reserved number of states. This problem
     * occurs explicit states are encoded by a set of DD variables, can only
     * encode (num-vars)^2 different states.
     * */
    public final static Problem DD_INSUFFICIENT_STATES = newProblem("dd-insufficient-states");

    /* TODO split off this part for Buechi model construction by SPOT to plugin,
     *  then move these options there. */

    /** An I/O problem called while calling SPOT or reading its output. */
    public final static Problem LTL2BA_SPOT_PROBLEM_IO = newProblem("ltl2ba-spot-problem-io");
    /** SPOT produced a non-zero error code at termination. */
    public final static Problem LTL2BA_SPOT_PROBLEM_EXIT_CODE = newProblem("ltl2ba-spot-problem-exit-code");

    /**
     * Generate new problem reading descriptions from automaton property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_AUTOMATON, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsAutomaton() {
    }
}
