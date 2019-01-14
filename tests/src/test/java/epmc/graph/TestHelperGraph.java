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

package epmc.graph;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.dd.DD;
import epmc.graph.CommonProperties;
import epmc.graph.SemanticsSMG;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.LogTest;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.UtilModelChecker;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class TestHelperGraph {
    public static GraphDD buildGraphDD(Options options, String modelFile) {
        try {
            Set<Object> graphProperties = Collections.singleton(CommonProperties.SEMANTICS);
            options.set(OptionsModelChecker.ENGINE, EngineDD.class);
            Model model = TestHelper.loadModel(options, modelFile);
            Set<Object> nodeProperties = new ObjectOpenHashSet<>();
            nodeProperties.add(CommonProperties.STATE);
            Set<Object> edgeProperties = new ObjectOpenHashSet<>();
            edgeProperties.add(CommonProperties.WEIGHT);
            return (GraphDD) UtilModelChecker.buildLowLevel(model, EngineDD.getInstance(), graphProperties, nodeProperties, edgeProperties);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static GraphExplicit exploreModelGraph(Model model) {
        GraphExplicit result;
        assert model != null;
        try {
            ModelChecker checker = new ModelChecker(model);
            result = exploreToGraph(model);
            checker.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static GraphExplicit exploreModelGraph(Model model, Set<Object> graphProperties,
            Set<Object> nodeProperties, Set<Object> edgeProperties) {
        GraphExplicit result;
        assert model != null;
        try {
            ModelChecker checker = new ModelChecker(model);
            result = exploreToGraph(model, graphProperties, nodeProperties, edgeProperties);
            checker.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static GraphExplicit exploreModelGraph(Model model, Set<Object> nodeProperties) {
        GraphExplicit result;
        assert model != null;
        try {
            ModelChecker checker = new ModelChecker(model);
            result = exploreToGraph(model, nodeProperties);
            checker.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static GraphExplicit exploreModelGraph(Options options, String modelFile) {
        try {
            Model model = TestHelper.loadModel(options, modelFile);
            return exploreModelGraph(model);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static ExploreStatistics exploreModel(Options options, String modelFile) {
        try {
            Model model = TestHelper.loadModel(options, modelFile);
            return exploreModel(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ExploreStatistics exploreModel(Model model) {
        assert model != null;
        Options options = Options.get();
        ExploreStatistics result;
        ModelChecker checker = new ModelChecker(model);
        LogTest log = options.get(OptionsMessages.LOG);
        log.getResults().clear();
        result = explore(model);
        checker.close();
        return result;
    }

    private static ExploreStatistics explore(Model model) {
        Engine engine = UtilOptions.getSingletonInstance(Options.get(),
                OptionsModelChecker.ENGINE);
        if (engine instanceof EngineExplicit) {
            long time = System.nanoTime();
            GraphExplicit modelGraph = exploreToGraph(model);            
            int numStates = 0;
            int numTransitions = 0;
            NodeProperty isState = modelGraph.getNodeProperty(CommonProperties.STATE);
            for (int node = 0; node < modelGraph.getNumNodes(); node++) {
                if (isState.getBoolean(node)) {
                    numStates++;
                }
                numTransitions += modelGraph.getNumSuccessors(node);
            }
            time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
            ExploreStatistics exploreStatistics = new ExploreStatistics(modelGraph.getNumNodes(), numStates, numTransitions);
            return exploreStatistics;
        } else if (engine instanceof EngineDD) {
            Set<Object> graphProperties = Collections.singleton(CommonProperties.SEMANTICS);
            Set<Object> nodeProperties = prepareNodeProperties(model);
            Set<Object> edgeProperties = prepareEdgeProperties(model);
            GraphDD modelGraphDD = (GraphDD) UtilModelChecker.buildLowLevel(model, EngineDD.getInstance(), graphProperties, nodeProperties, edgeProperties);
            DD space = modelGraphDD.getNodeSpace();
            BigInteger numNodes = space.countSat(modelGraphDD.getPresCube());
            DD states = modelGraphDD.getNodeProperty(CommonProperties.STATE);
            BigInteger numStates = space.and(states)
                    .countSatWith(modelGraphDD.getPresCube().clone());
            BigInteger numTransitions = space.and(modelGraphDD.getTransitions()).abstractExist(modelGraphDD.getActionCube().clone())
                    .countSatWith(modelGraphDD.getPresCube().
                            and(modelGraphDD.getNextCube()));
            ExploreStatistics statistics = new ExploreStatistics(numNodes, numStates, numTransitions);
            space.dispose();
            return statistics;
        } else {
            assert false;
            return null;
        }
    }

    private static Set<Object> prepareNodeProperties(Model model)
    {
        assert model != null;
        Set<Object> result = new LinkedHashSet<>();
        result.add(CommonProperties.STATE);
        if (!SemanticsSMG.isSMG(model.getSemantics())) {
            result.add(CommonProperties.PLAYER);
        }
        result.add(CommonProperties.STATE);
        return result;
    }

    private static Set<Object> prepareEdgeProperties(Model model)
    {
        assert model != null;
        Set<Object> result = new LinkedHashSet<>();
        result.add(CommonProperties.WEIGHT);
        return result;
    }

    public static GraphExplicit exploreToGraph(Model model, Set<Object> nodeProperties) {
        Set<Object> graphProperties = new LinkedHashSet<>();
        graphProperties.add(CommonProperties.SEMANTICS);
        Set<Object> edgeProperties = new LinkedHashSet<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        GraphExplicit graph = (GraphExplicit) UtilModelChecker.buildLowLevel(model, EngineExplicit.getInstance(),
                graphProperties, nodeProperties, edgeProperties);
        return graph;
    }

    public static GraphExplicit exploreToGraph(Model model) {
        Set<Object> graphProperties = new LinkedHashSet<>();
        graphProperties.add(CommonProperties.SEMANTICS);
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        Set<Object> edgeProperties = new LinkedHashSet<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        return exploreToGraph(model, graphProperties, nodeProperties, edgeProperties);
    }

    public static GraphExplicit exploreToGraph(Model model,
            Set<Object> graphProperties,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        GraphExplicit result = (GraphExplicit) UtilModelChecker.buildLowLevel(model, EngineExplicit.getInstance(), graphProperties, nodeProperties, edgeProperties);
        return result;
    }

    private TestHelperGraph() {
    }
}
