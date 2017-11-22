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

package epmc.constraintsolver.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in constraint solver plugin of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsConstraintsolver {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_CONSTRAINTSOLVER = "ProblemsConstraintsolver";

    /** Native memory allocation shall be performed but not enough memory is available. */
    public final static Problem CONSTRAINTSOLVER_INSUFFICIENT_NATIVE_MEMORY = newProblem("constraintsolver-insufficient-native-memory");

    /**
     * Generate new problem reading descriptions using correct resource bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_CONSTRAINTSOLVER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsConstraintsolver() {
    }
}
