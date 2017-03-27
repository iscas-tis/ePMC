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

package epmc.command;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.SemanticsSMG;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.GraphBuilderExplorer;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explorer.Explorer;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;

public class CommandTaskExplore implements CommandTask {
    public final static String IDENTIFIER = "explore";
    private ModelChecker modelChecker;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void executeInServer() {
        try {
			explore();
		} catch (EPMCException e) {
	        Log log = getLog();
			log.send(e);
		}
    }
    
    public void explore() throws EPMCException {
    	Model model = modelChecker.getModel();
    	Engine engine = modelChecker.getEngine();
        Log log = getLog();
        if (engine instanceof EngineExplicit) {
            long time = System.nanoTime();
            GraphExplicit modelGraph = exploreToGraph();
            int numStates = 0;
            int numTransitions = 0;
            NodeProperty isState = modelGraph.getNodeProperty(CommonProperties.STATE);
            for (int node = 0; node < modelGraph.getNumNodes(); node++) {
                modelGraph.queryNode(node);
                if (isState.getBoolean(node)) {
                    numStates++;
                }
                numTransitions += modelGraph.getNumSuccessors();
            }
            time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
            log.send(MessagesCommandExplore.EXPLORING_DONE, time);
            ExploreStatistics exploreStatistics = new ExploreStatistics(modelGraph.getNumNodes(), numStates, numTransitions);
            ModelCheckerResult result = new ModelCheckerResult(null, exploreStatistics);
            log.send(result);
        } else if (engine instanceof EngineDD) {
        	Set<Object> graphProperties = Collections.singleton(CommonProperties.SEMANTICS);
            Set<Object> nodeProperties = prepareNodeProperties(model);
            Set<Object> edgeProperties = prepareEdgeProperties(model);
            GraphDD modelGraphDD = (GraphDD) model.newLowLevel(engine, graphProperties, nodeProperties, edgeProperties);
            DD space = modelGraphDD.getNodeSpace();
            BigInteger numNodes = space.countSat(modelGraphDD.getPresCube());
            DD states = modelGraphDD.getNodeProperty(CommonProperties.STATE);
            BigInteger numStates = space.and(states)
                    .countSatWith(modelGraphDD.getPresCube().clone());
            BigInteger numTransitions = space.and(modelGraphDD.getTransitions()).abstractExist(modelGraphDD.getActionCube().clone())
                    .countSatWith(modelGraphDD.getPresCube().
                            and(modelGraphDD.getNextCube()));
            ExploreStatistics statistics = new ExploreStatistics(numNodes, numStates, numTransitions);
            ModelCheckerResult result = new ModelCheckerResult(null, statistics);
            space.dispose();
            log.send(result);
        } else {
            assert false;
        }
    }

    public GraphExplicit exploreToGraph() throws EPMCException {
        Log log = getLog();
        log.send(MessagesCommandExplore.EXPLORING);
        Set<Object> graphProperties = new LinkedHashSet<>();
        graphProperties.add(CommonProperties.SEMANTICS);
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        Set<Object> edgeProperties = new LinkedHashSet<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        GraphExplicit result = buildModelGraphExplicit(graphProperties, nodeProperties, edgeProperties);
        result.explore();
        return result;
    }

    private GraphExplicit buildModelGraphExplicit(Set<Object> graphProperties,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) throws EPMCException {
    	Model model = modelChecker.getModel();
        Explorer explorer = (Explorer) model.newLowLevel(EngineExplorer.getInstance(), graphProperties, nodeProperties, edgeProperties);
        GraphBuilderExplorer builder = new GraphBuilderExplorer();
        builder.setExplorer(explorer);
        builder.addDerivedGraphProperties(graphProperties);
        builder.addDerivedNodeProperties(nodeProperties);
        builder.addDerivedEdgeProperties(edgeProperties);
        builder.build();
        return builder.getGraph();
     }
    
    private Set<Object> prepareNodeProperties(Model model)
            throws EPMCException {
        assert model != null;
        Set<Object> result = new LinkedHashSet<>();
        result.add(CommonProperties.STATE);
        if (!SemanticsSMG.isSMG(model.getSemantics())) {
            result.add(CommonProperties.PLAYER);
        }
        result.add(CommonProperties.STATE);
        return result;
    }

    private Set<Object> prepareEdgeProperties(Model model)
            throws EPMCException {
        assert model != null;
        Set<Object> result = new LinkedHashSet<>();
        result.add(CommonProperties.WEIGHT);
        return result;
    }

    private Log getLog() {
    	return modelChecker.getModel().getContextValue().getOptions().get(OptionsMessages.LOG);
    }
}
