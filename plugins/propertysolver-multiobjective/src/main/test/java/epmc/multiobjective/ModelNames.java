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

package epmc.multiobjective;

/**
 * Filenames of test models for multiobjective solver.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelNames {
    /** Directory containing the model files. */
    private final static String PREFIX = "epmc/multiobjective/";

    public final static String MULTI_OBJECTIVE_SIMPLE = PREFIX + "multiObjectiveSimple.prism";
    public final static String MULTI_OBJECTIVE_SIMPLE_REWARDS = PREFIX + "multiObjectiveSimpleRewards.prism";
    public final static String DINNER_REDUCED_PROB_BOUNDED_1 = PREFIX + "dinnerReducedProb-bounded-1.prism";
    public final static String DINNER_REDUCED_BOUNDED_1 = PREFIX + "dinnerReduced-bounded-1.prism";
    public final static String ANDREA_BUG = PREFIX + "andreabug.prism";
    public final static String ANDREA_BUG_SECOND = PREFIX + "andreabugsecond.prism";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNames() {
    }
}
