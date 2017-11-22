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

package epmc.coalition.plugin;

import java.util.Map;

import epmc.coalition.dd.PropertySolverDDCoalition;
import epmc.coalition.explicit.PropertySolverExplicitCoalition;
import epmc.coalition.explicit.SolverNonStochasticJurdzinski;
import epmc.coalition.explicit.SolverNonStochasticMcNaughton;
import epmc.coalition.explicit.SolverQualitativeGadget;
import epmc.coalition.explicit.SolverQualitativeMcNaughton;
import epmc.coalition.graphsolver.GraphSolverIterativeCoalitionJava;
import epmc.coalition.graphsolver.GraphSolverIterativeCoalitionNative;
import epmc.coalition.options.OptionsCoalition;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeRealNonnegative;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

/**
 * Coalition plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationCoalition implements AfterOptionsCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-coalition";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsCoalition.OPTIONS_COALITION)
                .setIdentifier(OptionsCoalition.COALITION_CATEGORY)
                .build();
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        assert solvers != null;
        solvers.put(PropertySolverDDCoalition.IDENTIFIER, PropertySolverDDCoalition.class);
        solvers.put(PropertySolverExplicitCoalition.IDENTIFIER, PropertySolverExplicitCoalition.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        OptionTypeRealNonnegative typeReal = OptionTypeRealNonnegative.getInstance();
        OptionTypeEnum typeJurdzinskyLift = new OptionTypeEnum(
                OptionsCoalition.JurdzinskyChooseLiftNodes.class);
        OptionTypeEnum typeJurdzinskyLiftOrder = new OptionTypeEnum(
                OptionsCoalition.JurdzinskyLiftOrder.class);
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_SAME_COLOR_SHORTCUT)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES)
        .setType(typeJurdzinskyLift).setDefault(OptionsCoalition.JurdzinskyChooseLiftNodes.SUCCESSOR_CHANCED)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_JURDZINSKY_LIFT_ORDER)
        .setType(typeJurdzinskyLiftOrder).setDefault(OptionsCoalition.JurdzinskyLiftOrder.LIFO)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();		
        Map<String, Class<?>> stochasticQualitativeSolverMap = new OrderedMap<>();
        stochasticQualitativeSolverMap.put(SolverQualitativeGadget.IDENTIFIER, SolverQualitativeGadget.class);
        stochasticQualitativeSolverMap.put(SolverQualitativeMcNaughton.IDENTIFIER, SolverQualitativeMcNaughton.class);
        OptionTypeMap<Class<?>> stochasticSolverType = new OptionTypeMap<>(stochasticQualitativeSolverMap);
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_SOLVER)
        .setType(stochasticSolverType).setDefault(SolverQualitativeMcNaughton.class)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String, Class<?>> nonStochasticSolverMap = new OrderedMap<>();
        nonStochasticSolverMap.put(SolverNonStochasticJurdzinski.IDENTIFIER, SolverNonStochasticJurdzinski.class);
        nonStochasticSolverMap.put(SolverNonStochasticMcNaughton.IDENTIFIER, SolverNonStochasticMcNaughton.class);
        OptionTypeMap<Class<?>> nonStochasticSolverType = new OptionTypeMap<>(nonStochasticSolverMap);
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC)
        .setType(nonStochasticSolverType).setDefault(SolverNonStochasticMcNaughton.IDENTIFIER)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverIterativeCoalitionJava.IDENTIFIER, GraphSolverIterativeCoalitionJava.class);
        graphSolverMap.put(GraphSolverIterativeCoalitionNative.IDENTIFIER, GraphSolverIterativeCoalitionNative.class);
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_QUANTITATIVE_SCHEWE_SILENCE_INTERNAL)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsCoalition.OPTIONS_COALITION)
        .setIdentifier(OptionsCoalition.COALITION_QUANTITATIVE_SCHEWE_COMPARE_TOLERANCE)
        .setType(typeReal).setDefault(1E-8)
        .setCommandLine().setGui().setWeb()
        .setCategory(category)
        .build();
    }
}
