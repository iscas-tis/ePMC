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

package epmc.graphsolver.iterative;

import java.util.Map;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.iterative.java.BoundedCumulativeDiscountedJava;
import epmc.graphsolver.iterative.java.BoundedCumulativeJava;
import epmc.graphsolver.iterative.java.BoundedJava;
import epmc.graphsolver.iterative.java.BoundedReachabilityJava;
import epmc.graphsolver.iterative.java.UnboundedCumulativeJava;
import epmc.graphsolver.iterative.java.UnboundedReachabilityJava;
import epmc.graphsolver.iterative.natives.BoundedCumulativeDiscountedNative;
import epmc.graphsolver.iterative.natives.BoundedCumulativeNative;
import epmc.graphsolver.iterative.natives.BoundedNative;
import epmc.graphsolver.iterative.natives.BoundedReachabilityNative;
import epmc.graphsolver.iterative.natives.UnboundedCumulativeNative;
import epmc.graphsolver.iterative.natives.UnboundedLRANative;
import epmc.graphsolver.iterative.natives.UnboundedReachabilityNative;
import epmc.options.Category;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeRealNonnegative;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationGraphSolverIterative implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-graph-solver-iterative";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;

        Category category = options.addCategory()
                .setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
                .setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_CATEGORY)
                .setParent(OptionsGraphsolver.GRAPHSOLVER_CATEGORY)
                .build();
        OptionTypeRealNonnegative typeRealNonnegative = OptionTypeRealNonnegative.getInstance();

        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        .setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD)
        .setType(new OptionTypeEnum(IterationMethod.class))
        .setDefault(IterationMethod.GAUSS_SEIDEL)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        .setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION)
        .setType(new OptionTypeEnum(IterationStopCriterion.class))
        .setDefault(IterationStopCriterion.ABSOLUTE)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        .setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE)
        .setType(typeRealNonnegative)
        .setDefault("1.0E-10")
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(BoundedCumulativeJava.IDENTIFIER, BoundedCumulativeJava.class);
        graphSolverMap.put(BoundedCumulativeDiscountedJava.IDENTIFIER, BoundedCumulativeDiscountedJava.class);
        graphSolverMap.put(UnboundedCumulativeJava.IDENTIFIER, UnboundedCumulativeJava.class);
        graphSolverMap.put(UnboundedReachabilityJava.IDENTIFIER, UnboundedReachabilityJava.class);
        graphSolverMap.put(BoundedReachabilityJava.IDENTIFIER, BoundedReachabilityJava.class);
        graphSolverMap.put(BoundedJava.IDENTIFIER, BoundedJava.class);
        graphSolverMap.put(BoundedCumulativeNative.IDENTIFIER, BoundedCumulativeNative.class);
        graphSolverMap.put(BoundedCumulativeDiscountedNative.IDENTIFIER, BoundedCumulativeDiscountedNative.class);
        graphSolverMap.put(UnboundedCumulativeNative.IDENTIFIER, UnboundedCumulativeNative.class);
        graphSolverMap.put(UnboundedLRANative.IDENTIFIER, UnboundedLRANative.class);
        graphSolverMap.put(UnboundedReachabilityNative.IDENTIFIER, UnboundedReachabilityNative.class);
        graphSolverMap.put(BoundedReachabilityNative.IDENTIFIER, BoundedReachabilityNative.class);
        graphSolverMap.put(BoundedNative.IDENTIFIER, BoundedNative.class);
    }
}
