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

package epmc.kretinsky.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsKretinsky {
    private final static String ERROR_KRETINSKY = "ErrorKretinsky";

    public final static Problem KRETINSKY_PO_OPT_CMD_LINE_SET_MULTIPLE = newProblem("rddl-po-opt-cmd-line-set-multiple");
    public final static Problem KRETINSKY_PO_INV_PGR_OPT_VALUE = newProblem("kretinsky-po-inv-prg-opt-value");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(ERROR_KRETINSKY, ProblemsKretinsky.class, name);
    }

    private ProblemsKretinsky() {
    }
}
