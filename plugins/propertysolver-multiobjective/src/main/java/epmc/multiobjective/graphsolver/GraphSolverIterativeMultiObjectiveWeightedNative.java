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

package epmc.multiobjective.graphsolver;

import java.nio.ByteBuffer;

import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.util.ProblemsUtil;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayInteger;
import epmc.value.ValueContentMemory;

public final class GraphSolverIterativeMultiObjectiveWeightedNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-multiobjective-weighted-native";
    
    private static final class IterationNative {
        native static int double_mdp_multiobjectiveweighted_jacobi(int relative,
                double precision, int numStates,
                ByteBuffer stateBounds, ByteBuffer nondetBounds, ByteBuffer targets,
                ByteBuffer weights, ByteBuffer stopRewards, ByteBuffer transRewardsMem,
                ByteBuffer values, ByteBuffer scheduler);

        native static int double_mdp_multiobjectiveweighted_gaussseidel(int relative,
                double precision, int numStates,
                ByteBuffer stateBounds, ByteBuffer nondetBounds, ByteBuffer targets,
                ByteBuffer weights, ByteBuffer stopRewards, ByteBuffer transRewards,
                ByteBuffer values, ByteBuffer scheduler);
        
        private final static boolean loaded =
                JNATools.registerLibrary(IterationNative.class, "valueiteration");
        
        private final static int EPMC_ERROR_SUCCESS = 0;
        private final static int EPMC_ERROR_OUT_OF_ByteBuffer = 1;
    }

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArray inputValues;
    private ValueArray outputValues;
    private ValueArrayInteger scheduler;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setGraphSolverObjective(GraphSolverObjectiveExplicit objective) {
    	this.objective = objective;
        origGraph = objective.getGraph();
    }

    @Override
    public boolean canHandle() {
    	if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveWeighted)) {
            return false;
        }
    	Semantics semantics = origGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
    	if (!SemanticsMDP.isMDP(semantics)) {
    		return false;
    	}
        Options options = origGraph.getOptions();
        Type typeWeight = TypeWeight.get(origGraph.getContextValue());
        if (!(options.getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeDouble.isDouble(typeWeight))) {
        	return false;
        }

        return true;
    }

    @Override
    public void solve() throws EPMCException {
    	prepareIterGraph();
    	multiobjectiveWeighted();
        prepareResultValues();
    }

    private void prepareIterGraph() throws EPMCException {
        assert origGraph != null;
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setForNative(true);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        inputValues = objectiveMultiObjectiveWeighted.getValues();
    }

    private void prepareResultValues() throws EPMCException {
    	this.outputValues = inputValues;
    	objective.setResult(outputValues);
    }

    private void multiobjectiveWeighted() throws EPMCException {
        Options options = iterGraph.getOptions();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        Value cumulativeTransitionRewards = objectiveMultiObjectiveWeighted.getTransitionRewards();
        Value stopStateRewards = objectiveMultiObjectiveWeighted.getStopStateReward();
        ContextValue contextValue = origGraph.getContextValue();
        Type typeWeight = TypeWeight.get(origGraph.getContextValue());
        boolean useNative = options.getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeDouble.isDouble(typeWeight);
        scheduler = useNative
        		? UtilValue.newArray(TypeInteger.get(contextValue).getTypeArrayNative(), origGraph.computeNumStates())
        	    : UtilValue.newArray(TypeInteger.get(contextValue).getTypeArray(), origGraph.computeNumStates());
        objectiveMultiObjectiveWeighted.setScheduler(scheduler);
        inputValues = objectiveMultiObjectiveWeighted.getValues();
        if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectiveweightedJacobiNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectiveweightedGaussseidelNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else {
            assert false;
        }
    }
    
    /* auxiliary methods */
    
    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }
    
    private static boolean isSparseMDPNative(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        GraphExplicitSparseAlternate sparseNondet = asSparseNondet(graph);
        return sparseNondet.isNative();
    }

    
    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    /* native call to iteration algorithms */    
    
    private static void mdpMultiobjectiveweightedJacobiNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, Value scheduler) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        ByteBuffer stateBounds = graph.getStateBoundsNative();
        ByteBuffer nondetBounds = graph.getNondetBoundsNative();
        ByteBuffer targets = graph.getTargetsNative();
        ByteBuffer weights = ValueContentMemory.getMemory(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ByteBuffer valuesMem = ValueContentMemory.getMemory(values);
        ByteBuffer stopRewardsMem = ValueContentMemory.getMemory(stopRewards);
        ByteBuffer schedulerMem = ValueContentMemory.getMemory(scheduler);
        ByteBuffer transRewardsMem = ValueContentMemory.getMemory(transRewards);

        int code = IterationNative.double_mdp_multiobjectiveweighted_jacobi(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_ByteBuffer, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpMultiobjectiveweightedGaussseidelNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, Value scheduler) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        ByteBuffer stateBounds = graph.getStateBoundsNative();
        ByteBuffer nondetBounds = graph.getNondetBoundsNative();
        ByteBuffer targets = graph.getTargetsNative();
        ByteBuffer weights = ValueContentMemory.getMemory(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ByteBuffer valuesMem = ValueContentMemory.getMemory(values);
        ByteBuffer stopRewardsMem = ValueContentMemory.getMemory(stopRewards);
        ByteBuffer schedulerMem = ValueContentMemory.getMemory(scheduler);
        ByteBuffer transRewardsMem = ValueContentMemory.getMemory(transRewards);

        int code = IterationNative.double_mdp_multiobjectiveweighted_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_ByteBuffer, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;        
    }
}
