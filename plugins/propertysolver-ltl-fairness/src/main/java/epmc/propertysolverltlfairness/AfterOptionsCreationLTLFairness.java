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

package epmc.propertysolverltlfairness;

import java.util.Map;

import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationLTLFairness implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-ltl-fairness";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        assert solvers != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        solvers.put(PropertySolverDDLTLFairness.IDENTIFIER, PropertySolverDDLTLFairness.class);
        solvers.put(PropertySolverExplicitLTLFairness.IDENTIFIER, PropertySolverExplicitLTLFairness.class);
        options.addOption().setBundleName(OptionsLTLFairness.OPTIONS_LTL_FAIRNESS)
        .setIdentifier(OptionsLTLFairness.LTL_FAIRNESS_SCC_SKIP_TRANSIENT)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb().build();
    }
}
