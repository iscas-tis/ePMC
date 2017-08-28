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

import java.util.Map;

import epmc.graph.SchedulerPrinter;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveScheduledJava;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveScheduledJavaDouble;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveScheduledNative;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveWeightedJava;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveWeightedJavaDouble;
import epmc.multiobjective.graphsolver.GraphSolverIterativeMultiObjectiveWeightedNative;
import epmc.options.Category;
import epmc.options.OptionTypeRealNonnegative;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationMultiObjective implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-multi-objective";
    private final static String MIN_INCREASE = "1E-7";
    private final static String MIN_NONZERO = "1E-8";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        solvers.put(PropertySolverExplicitMultiObjective.IDENTIFIER, PropertySolverExplicitMultiObjective.class);
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveWeightedJava.IDENTIFIER, GraphSolverIterativeMultiObjectiveWeightedJava.class);
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveWeightedJavaDouble.IDENTIFIER, GraphSolverIterativeMultiObjectiveWeightedJavaDouble.class);
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveWeightedNative.IDENTIFIER, GraphSolverIterativeMultiObjectiveWeightedNative.class);
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveScheduledJava.IDENTIFIER, GraphSolverIterativeMultiObjectiveScheduledJava.class);
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveScheduledJavaDouble.IDENTIFIER, GraphSolverIterativeMultiObjectiveScheduledJavaDouble.class);
        graphSolverMap.put(GraphSolverIterativeMultiObjectiveScheduledNative.IDENTIFIER, GraphSolverIterativeMultiObjectiveScheduledNative.class);
        Category category = options.addCategory()
                .setBundleName(OptionsMultiObjective.OPTIONS_MULTI_OBJECTIVE)
                .setIdentifier(OptionsMultiObjective.MULTI_OBJECTIVE_CATEGORY)
                .build();
        options.addOption().setBundleName(OptionsMultiObjective.OPTIONS_MULTI_OBJECTIVE)
        .setIdentifier(OptionsMultiObjective.MULTI_OBJECTIVE_MIN_INCREASE)
        .setCategory(category)
        .setCommandLine().setGui().setWeb()
        .setType(OptionTypeRealNonnegative.getInstance())
        .setDefault(MIN_INCREASE)
        .build();
        options.addOption().setBundleName(OptionsMultiObjective.OPTIONS_MULTI_OBJECTIVE)
        .setIdentifier(OptionsMultiObjective.MULTI_OBJECTIVE_MIN_NONZERO_WEIGHT)
        .setCategory(category)
        .setCommandLine().setGui().setWeb()
        .setType(OptionTypeRealNonnegative.getInstance())
        .setDefault(MIN_NONZERO)
        .build();
        Map<String,Class<? extends SchedulerPrinter>> schedulerPrinters = options.get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS);
        assert schedulerPrinters != null;
    }
}
