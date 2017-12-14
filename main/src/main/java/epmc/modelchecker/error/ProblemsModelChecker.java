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

package epmc.modelchecker.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in model checker part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsModelChecker {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_MODEL_CHECKER = "ProblemsModelChecker";
    /** No property solver for the given property is available. */
    public final static Problem NO_SOLVER_AVAILABLE = newProblem("no-solver-available");
    /** No low-level builder available for combination of model and engine. */
    public final static Problem NO_LOW_LEVEL_AVAILABLE = newProblem("no-low-level-available");
    /** Local model file not found. */
    public static final Problem FILE_NOT_EXISTS = newProblem("file-not-exists");
    // TODO probably split into problem for const, property, formula
    /** Property with same name, formula, or constant defined twice */
    public final static Problem DEFINED_TWICE = newProblem("defined-twice");
    /** Constant was undefined while trying to model check. */
    public final static Problem CONST_UNDEFINED = newProblem("const-undefined");
    /** Constant definition is non-constant (e.g. depends on model variables). */
    public final static Problem CONST_NON_CONST = newProblem("const-non-const");
    /** There are cyclic dependencies between constants. */
    public final static Problem CONST_CYCLIC = newProblem("const-cyclic");

    /**
     * Generate new problem reading descriptions from EPMC property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_MODEL_CHECKER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsModelChecker() {
    }
}
