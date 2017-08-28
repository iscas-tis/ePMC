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

package epmc.propertysolver;

import java.util.Map;

import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationReachability implements AfterOptionsCreation {
    // set identifier for the class
    private final static String IDENTIFIER = "after-options-creation-reachability";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        // get the solver map from options
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);

        // put our self-defined solvers into the available solvers (both explicit and dd implementation)
        solvers.put(PropertySolverExplicitReachability.IDENTIFIER, PropertySolverExplicitReachability.class);
        solvers.put(PropertySolverDDReachability.IDENTIFIER, PropertySolverDDReachability.class);
    }
}
