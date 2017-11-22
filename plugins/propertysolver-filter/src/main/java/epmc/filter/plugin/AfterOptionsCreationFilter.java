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

package epmc.filter.plugin;

import java.util.Map;

import epmc.filter.propertysolver.PropertySolverDDFilter;
import epmc.filter.propertysolver.PropertySolverExplicitFilter;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

/**
 * Filter plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationFilter implements AfterOptionsCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-filter";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        assert solvers != null;
        solvers.put(PropertySolverDDFilter.IDENTIFIER, PropertySolverDDFilter.class);
        solvers.put(PropertySolverExplicitFilter.IDENTIFIER, PropertySolverExplicitFilter.class);
    }
}
