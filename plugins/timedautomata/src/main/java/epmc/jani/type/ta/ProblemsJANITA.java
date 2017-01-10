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

package epmc.jani.type.ta;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in TA part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANITA {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_TA = "ProblemsJANITA";

	public static final Problem JANI_TA_ONLY_ONE_DESTINATIONS = newProblem("jani-ta-only-one-destination");
	public static final Problem JANI_TA_NO_PROBABILITIES = newProblem("jani-ta-no-probabilities");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_TA, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANITA() {
    }
}
