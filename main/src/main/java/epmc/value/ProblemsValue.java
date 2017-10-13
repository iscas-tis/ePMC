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

package epmc.value;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Problems which can occur in the context of value package.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsValue {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_VALUE = "ProblemsValue";

    public final static Problem OPTIONS_NO_OPERATOR_AVAILABLE = newProblem("value-no-operator-available");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter may not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_VALUE, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsValue() {
    }
}
