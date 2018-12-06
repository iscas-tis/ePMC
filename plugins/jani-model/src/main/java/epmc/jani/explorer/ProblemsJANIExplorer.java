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

package epmc.jani.explorer;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in explorer part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIExplorer {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_EXPLORER = "ProblemsJANIExplorer";

    /** Global variable was written multiple times in explicit-state model explorer. */
    public static final Problem JANI_EXPLORER_GLOBAL_MULTIPLE = newProblem("jani-explorer-global-multiple");
    /** Deadlock state reached during explicit state space exploration. */
    public static final Problem JANI_EXPLORER_DEADLOCK = newProblem("jani-explorer-deadlock");
    /** Invalid assignment to variable during initial states computation. */
    public static final Problem JANI_EXPLORER_TRANSITION_INVALID_INITIAL_STATE = newProblem("jani-explorer-transition-invalid-initial-state");
    /** Invalid assignment to variable during transition. */
    public static final Problem JANI_EXPLORER_TRANSITION_INVALID_ASSIGNMENT = newProblem("jani-explorer-transition-invalid-assignment");
    /** Probabilities do not sum up to one. */
    public static final Problem JANI_EXPLORER_PROBABILIY_SUM_NOT_ONE = newProblem("jani-explorer-probability-sum-not-one");
    /** Negative rate or probability. */
    public static final Problem JANI_EXPLORER_NEGATIVE_WEIGHT = newProblem("jani-explorer-negative-weight");
    /** Infinitely many initial states. */
    public static final Problem JANI_EXPLORER_INFINITELY_MANY_INITIAL_STATES = newProblem("jani-explorer-infinitely-many-initial-states");
    /** Cannot use brute-force enumerator because of unbounded variable values. */
    public static final Problem JANI_EXPLORER_INITIAL_STATES_BRUTE_FORCE_UNBOUNDED = newProblem("jani-explorer-initial-states-brute-force-unbounded-variables");
    /** A certain constant should be defined for analysis but is not */
    public static final Problem JANI_EXPLORER_UNDEFINED_CONSTANT = newProblem("jani-explorer-undefined-constant");
    /** There are undefined constants in the model. */
    public static final Problem JANI_EXPLORER_UNDEFINED_CONSTANTS = newProblem("jani-explorer-undefined-constants");
    
    /**
     * Create new problem object using plugin resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_JANI_EXPLORER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIExplorer() {
    }
}
