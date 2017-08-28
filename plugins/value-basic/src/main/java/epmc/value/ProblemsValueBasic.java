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
 * Class collecting problems potentially occurring in value basic plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsValueBasic {
    /** Base name of resource file containing module problem descriptions. */
    public final static String PROBLEMS_VALUE_BASIC = "ProblemsValueBasic";
    /** Two values are to be compared. However, they are not equal, but also
     * none of them is smaller or larger than the other.
     * */
    public final static Problem VALUES_INCOMPARABLE = newProblem("values-incomparable");
    /** Given string does not represent a valid value of given type. */
    public final static Problem VALUES_STRING_INVALID_VALUE = newProblem("value-string-invalid-value");
    public final static Problem VALUES_UNSUPPORTED_OPERATION = newProblem("value-unsupported-operation");

    /**
     * Create new problem object using module resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_VALUE_BASIC, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsValueBasic() {
    }
}
