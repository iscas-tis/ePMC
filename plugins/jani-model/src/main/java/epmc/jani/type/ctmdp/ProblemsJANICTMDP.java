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

package epmc.jani.type.ctmdp;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in CTMDP part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANICTMDP {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_CTMDP = "ProblemsJANICTMDP";

    public static final Problem JANI_CTMDP_EDGE_REQUIRES_RATE = newProblem("jani-ctmdp-edge-requires-rate");
    /** Time progress conditions are disallowed in CTMDPs. */
    public static final Problem JANI_CTMDP_DISALLOWED_TIME_PROGRESSES = newProblem("jani-ctmdp-disallowed-time-progresses");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_JANI_CTMDP, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANICTMDP() {
    }
}
