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

package epmc.propertysolver.ltllazy;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsLTLLazy {
    public final static String ERROR_LTL_LAZY = "ErrorLTLLazy";
    public final static Problem LTL_LAZY_COULDNT_DECIDE = newProblem("ltl-lazy-couldnt-decide");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(ERROR_LTL_LAZY, name);
    }

    private ProblemsLTLLazy() {
    }
}
