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

package epmc.util;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsUtil {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_UTIL = "ProblemsUtil";
    /** Native memory allocation shall be performed but not enough memory is available. */
    public final static Problem INSUFFICIENT_NATIVE_MEMORY = newProblem("insufficient-native-memory");
    public final static Problem CANNOT_UNESCAPE = newProblem("cannot-unescape");

    /**
     * Generate new problem reading descriptions from plugin property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_UTIL, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsUtil() {
    }
}
