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

package epmc.automaton.hoa;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsHoa {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_HOA = "ProblemsHoa";

    public final static Problem HOA_INVALID_ACC_SET = newProblem("hoa-invalid-acc-set");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_HOA, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsHoa() {
    }
}
