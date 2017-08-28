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

package epmc.constraintsolver.plugin;

import java.util.Map;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.options.Category;
import epmc.options.OptionTypeStringListSubset;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationConstraintsolver implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-constraintsolver";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;

        Category category = options.addCategory()
                .setBundleName(OptionsConstraintsolver.OPTIONS_CONSTRAINTSOLVER)
                .setIdentifier(OptionsConstraintsolver.CONSTRAINTSOLVER_CATEGORY)
                .build();

        Map<String,Class<?>> solvers = new OrderedMap<>();
        options.set(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS, solvers);
        OptionTypeStringListSubset<Class<?>> solverType = new OptionTypeStringListSubset<>(solvers);
        options.addOption().setBundleName(OptionsConstraintsolver.OPTIONS_CONSTRAINTSOLVER)
        .setIdentifier(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER)
        .setType(solverType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }

}
