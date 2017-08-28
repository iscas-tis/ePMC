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

package epmc.dd;

import epmc.error.Problem;
import epmc.error.UtilError;

// TODO content should be moved to according DD plugins
public final class ProblemsDD {
    private final static String PROBLEMS_DD = "ProblemsDD";
    /** There is insufficient memory to perform an operation in a native DD library. */
    public final static Problem INSUFFICIENT_NATIVE_MEMORY = newProblem("insufficient-native-memory");
    public final static Problem BUDDY_NATIVE_LOAD_FAILED = newProblem("buddy-native-load-failed");
    public final static Problem CACBDD_NATIVE_LOAD_FAILED = newProblem("cacbdd-native-load-failed");
    public final static Problem CUDD_NATIVE_LOAD_FAILED = newProblem("cudd-native-load-failed");
    public final static Problem MEDDLY_NATIVE_LOAD_FAILED = newProblem("meddly-native-load-failed");
    public final static Problem SYLVAN_NATIVE_LOAD_FAILED = newProblem("sylvan-native-load-failed");
    public final static Problem NO_BDD_LIBRARY_AVAILABLE = newProblem("no-bdd-library-available");
    public final static Problem NO_MTBDD_LIBRARY_AVAILABLE = newProblem("no-mtbdd-library-available");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(PROBLEMS_DD, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsDD() {
    }
}
