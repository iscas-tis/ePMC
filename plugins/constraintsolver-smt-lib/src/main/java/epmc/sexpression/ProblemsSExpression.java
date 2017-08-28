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

package epmc.sexpression;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsSExpression {
    public final static String ERROR_S_EXPRESSION = "ErrorSExpression";
    public final static Problem SEXPRESSION_UNEXPECTED_END_OF_INPUT = newProblem("sexpression-unexpected-end-of-input");
    public final static Problem SEXPRESSION_UNEXPECTED_CLOSING_BRACE = newProblem("sexpression-unexpected-closing-brace");
    public final static Problem SEXPRESSION_END_OF_INPUT_EXPECTED = newProblem("sexpression-end-of-input-expected");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(ERROR_S_EXPRESSION, name);
    }

    private ProblemsSExpression() {
    }
}
