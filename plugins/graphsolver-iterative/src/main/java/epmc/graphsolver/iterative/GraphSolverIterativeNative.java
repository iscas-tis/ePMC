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

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitModifier;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulativeDiscounted;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.JNATools;
import epmc.util.ProblemsUtil;
import epmc.util.StopWatch;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import epmc.value.ValueReal;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The routines provided here only work in the case that reals are implemented
 * using IEEE doubles. They are implemented in native code, and should run
 * relatively fast.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphSolverIterativeNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-native";
    
    private static final class IterationNative {
        native static int double_dtmc_bounded(int bound, int numStates,
                int[] stateBounds, int[] targets, double[] weights,
                double[] values);

        native static int double_dtmc_bounded_cumulative(int bound, int numStates,
                int[] stateBounds, int[] targets, double[] weights,
                double[] values, double[] cumul);

        native static int double_dtmc_bounded_cumulative_discounted(int bound, double discount, int numStates,
                int[] stateBounds, int[] targets, double[] weights,
                double[] values, double[] cumul);

        native static int double_dtmc_unbounded_jacobi(int relative,
                double precision, int numStates, int[] stateBounds,
                int[] targets, double[] weights, double[] values);

        native static int double_dtmc_unbounded_gaussseidel(int relative,
                double precision, int numStates, int[] stateBounds,
                int[] targets, double[] weights, double[] values);

        native static int double_dtmc_unbounded_cumulative_jacobi(int relative,
                double precision, int numStates, int[] stateBounds,
                int[] targets, double[] weights, double[] values, double[] cumul);

        native static int double_dtmc_unbounded_cumulative_gaussseidel(int relative,
                double precision, int numStates, int[] stateBounds,
                int[] targets, double[] weights, double[] values, double[] cumul);

        native static int double_ctmc_bounded(double[] fg, int left, int right,
                int numStates, int[] stateBounds, int[] targets,
                double[] weights, double[] values);

        /*
        native static int double_ctmc_bounded_cumulative(double[] fg, int left, int right,
                int numStates, int[] stateBounds, int[] targets,
                double[] weights, double[] values, double[] cumul);
        */
        
        native static int double_mdp_unbounded_jacobi(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values);

        native static int double_mdp_unbounded_gaussseidel(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values);

        native static int double_mdp_unbounded_cumulative_jacobi(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values, double[] cumul);

        native static int double_mdp_unbounded_cumulative_gaussseidel(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values, double[] cumul);
        
        native static int double_mdp_bounded(int bound, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values);

        native static int double_mdp_bounded_cumulative(int bound, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values, double[] cumul);

        native static int double_mdp_bounded_cumulative_discounted(int bound, double discount, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, int min, double[] values, double[] cumul);

        private final static boolean loaded =
                JNATools.registerLibrary(IterationNative.class, "valueiteration");
        
        private final static int EPMC_ERROR_SUCCESS = 0;
        private final static int EPMC_ERROR_OUT_OF_MEMORY = 1;
    }

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private int numIterations;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;
	private ValueArrayAlgebra cumulativeStateRewards;

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
    	assert origGraph != null;
        Semantics semantics = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsCTMC.isCTMC(semantics)
         && !SemanticsDTMC.isDTMC(semantics)
         && !SemanticsMDP.isMDP(semantics)) {
        	return false;
        }
    	if (true 
    			&& !(objective instanceof GraphSolverObjectiveExplicitBoundedCumulative)
    			&& !(objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted)
    			&& !(objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative)
    			&& !(objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)
    			) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() throws EPMCException {
    	prepareIterGraph();
        if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative) {
        	boundedCumulative();
        } else if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) {
        	boundedCumulativeDiscounted();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            unboundedReachability();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            unboundedCumulative();
        } else {
            assert false;
        }
        prepareResultValues();
    }

    private void prepareIterGraph() throws EPMCException {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative);
        boolean embed = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        List<BitSet> sinks = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
        	GraphSolverObjectiveExplicitUnboundedCumulative objectiveUnboundedCumulative = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        	sinks = objectiveUnboundedCumulative.getSinks();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
        	sinks = new ArrayList<>();
        	GraphSolverObjectiveExplicitUnboundedReachability unbounded = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        	if (unbounded.getZeroSet() != null) {
        		sinks.add(unbounded.getZeroSet());
        	}
        	sinks.add(unbounded.getTarget());
        }
        
        if (sinks != null) {
            builder.addSinks(sinks);
        }
        builder.setUniformise(uniformise);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        Value unifRate = newValueWeight();
        if (embed) {
            GraphExplicitModifier.embed(iterGraph);
        } else if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
        }
        BitSet targets = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
        	GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        	targets = objectiveUnboundedReachability.getTarget();
        }
        if (targets != null) {
            assert this.inputValues == null;
            int numStates = iterGraph.computeNumStates();
            this.inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
            for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                int iterNode = builder.inputToOutputNode(origNode);
                if (iterNode < 0) {
                    continue;
                }
                this.inputValues.set(targets.get(origNode) ? 1 : 0, iterNode);
            }
        }
        
        cumulativeStateRewards = null;
        if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative) {
        	GraphSolverObjectiveExplicitBoundedCumulative objectiveBoundedCumulative = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
        	cumulativeStateRewards = objectiveBoundedCumulative.getStateRewards();
        } else if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) {
        	GraphSolverObjectiveExplicitBoundedCumulativeDiscounted objectiveBoundedCumulativeDiscounted = (GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) objective;
        	cumulativeStateRewards = objectiveBoundedCumulativeDiscounted.getStateRewards();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
        	GraphSolverObjectiveExplicitUnboundedCumulative objectiveUnboundedCumulative = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        	cumulativeStateRewards = objectiveUnboundedCumulative.getStateRewards();
        }
        if (cumulativeStateRewards != null) {
        	if (SemanticsNonDet.isNonDet(semanticsType)) {
        		// TODO
        	} else {
        		int size = 0;
                for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                    int iterNode = builder.inputToOutputNode(origNode);
                    if (iterNode < 0) {
                        continue;
                    }
                    size++;
                }
        		ValueArrayAlgebra cumulativeStateRewardsNew = UtilValue.newArray(cumulativeStateRewards.getType(), size);
        		Value value = cumulativeStateRewards.getType().getEntryType().newValue();
                for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                    int iterNode = builder.inputToOutputNode(origNode);
                    if (iterNode < 0) {
                        continue;
                    }
                    cumulativeStateRewards.get(value, origNode);
                    cumulativeStateRewardsNew.set(value, iterNode);
                }
                cumulativeStateRewards = cumulativeStateRewardsNew;
        	}
        }
    }

    private void prepareResultValues() throws EPMCException {
    	TypeAlgebra typeWeight = TypeWeight.get();
    	TypeArrayAlgebra typeArrayWeight = typeWeight.getTypeArray();
    	this.outputValues = UtilValue.newArray(typeArrayWeight, origGraph.computeNumStates());
    	Value val = typeWeight.newValue();
    	int origStateNr = 0;
    	for (int i = 0; i < origGraph.getNumNodes(); i++) {
    		int iterState = builder.inputToOutputNode(i);
    		if (iterState == -1) {
    			continue;
    		}
    		inputValues.get(val, iterState);
    		outputValues.set(val, origStateNr);
    		origStateNr++;
    	}
    	objective.setResult(outputValues);
    }

    private void unboundedReachability() throws EPMCException {
        Options options = Options.get();
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        numIterations = 0;
        GraphSolverObjectiveExplicitUnboundedReachability graphSolverObjectiveUnbounded = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        boolean min = graphSolverObjectiveUnbounded.isMin();
        double precision = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            dtmcUnboundedJacobiNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision);
        } else if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            dtmcUnboundedGaussseidelNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpUnboundedJacobiNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpUnboundedGaussseidelNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision);  
        } else {
            assert false : iterGraph.getClass();
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations,
                timer.getTimeSeconds());
    }

    private void unboundedCumulative() throws EPMCException {
        Options options = Options.get();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        numIterations = 0;
        GraphSolverObjectiveExplicitUnboundedCumulative graphSolverObjectiveUnbounded = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        boolean min = graphSolverObjectiveUnbounded.isMin();
        double precision = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            dtmcUnboundedCumulativeJacobiNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            dtmcUnboundedCumulativeGaussseidelNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
        	mdpUnboundedCumulativeJacobiNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpUnboundedCumulativeGaussseidelNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else {
            assert false;
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations,
                timer.getTimeSeconds());
    }
    
    private void boundedCumulative() throws EPMCException {
        assert iterGraph != null;
        GraphSolverObjectiveExplicitBoundedCumulative objectiveBoundedCumulative = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
        ValueInteger time = ValueInteger.asInteger(objectiveBoundedCumulative.getTime());
        assert time.getInt() >= 0;
        numIterations = time.getInt();
        boolean min = objectiveBoundedCumulative.isMin();
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        if (isSparseMarkovNative(iterGraph)) {
            dtmcBoundedCumulativeNative(time.getInt(), asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (isSparseMDPNative(iterGraph)) {
            mdpBoundedCumulativeNative(time.getInt(), asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
        } else {
            assert false : iterGraph.getClass();
        }
    }

    private void boundedCumulativeDiscounted() throws EPMCException {
        assert iterGraph != null;
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        GraphSolverObjectiveExplicitBoundedCumulativeDiscounted objectiveBoundedCumulativeDiscounted = (GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) objective;
        ValueInteger time = ValueInteger.asInteger(objectiveBoundedCumulativeDiscounted.getTime());
        assert time.getInt() >= 0;
        ValueReal discount = objectiveBoundedCumulativeDiscounted.getDiscount();
        assert discount != null;
        assert ValueReal.isReal(discount) || ValueInteger.isInteger(discount);
        numIterations = time.getInt();
        boolean min = objectiveBoundedCumulativeDiscounted.isMin();
        if (isSparseMarkovNative(iterGraph)) {
            dtmcBoundedCumulativeDiscountedNative(time.getInt(), discount, asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (isSparseMDPNative(iterGraph)) {
            mdpBoundedCumulativeDiscountedNative(time.getInt(), discount, asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
        } else {
            assert false;
        }
    }

    

    /* auxiliary methods */
    
    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }
    
    private static boolean isSparseMarkov(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparse;
    }
    
    private static boolean isSparseMDPNative(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        return true;
    }
    
    private static boolean isSparseMarkovNative(GraphExplicit graph) {
        if (!isSparseMarkov(graph)) {
            return false;
        }
        return true;
    }

    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    private static GraphExplicitSparse asSparseMarkov(GraphExplicit graph) {
        return (GraphExplicitSparse) graph;
    }
    
    /* implementation/native call of/to iteration algorithms */    
    
    private static void dtmcUnboundedJacobiNative(GraphExplicitSparse graph,
            Value values,
            IterationStopCriterion stopCriterion, double tolerance) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);

        int code = IterationNative.double_dtmc_unbounded_jacobi(relative, tolerance, numStates, stateBounds, targets, weights, valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void dtmcUnboundedGaussseidelNative(
            GraphExplicitSparse graph, Value values,
            IterationStopCriterion stopCriterion, double tolerance) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);

        int code = IterationNative.double_dtmc_unbounded_gaussseidel(relative, tolerance, numStates, stateBounds, targets, weights, valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }
    
    private static void dtmcUnboundedCumulativeJacobiNative(GraphExplicitSparse graph,
            Value values,
            IterationStopCriterion stopCriterion, double tolerance, Value cumul)
                    throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        
        int code = IterationNative.double_dtmc_unbounded_cumulative_jacobi(relative, tolerance, numStates, stateBounds, targets, weights, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void dtmcUnboundedCumulativeGaussseidelNative(
            GraphExplicitSparse graph, Value values,
            IterationStopCriterion stopCriterion, double tolerance, Value cumul)
                    throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        
        int code = IterationNative.double_dtmc_unbounded_cumulative_gaussseidel(relative, tolerance, numStates, stateBounds, targets, weights, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }
    
    private static void dtmcBoundedCumulativeNative(int bound,
            GraphExplicitSparse graph, Value values, Value cumul)
                    throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = IterationNative.double_dtmc_bounded_cumulative(bound, numStates, stateBounds, targets, weights, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void dtmcBoundedCumulativeDiscountedNative(int bound,
            ValueReal discount, GraphExplicitSparse graph, Value values, Value cumul)
                    throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        double discountDouble = discount.getDouble();

        int code = IterationNative.double_dtmc_bounded_cumulative_discounted(bound, discountDouble, numStates, stateBounds, targets, weights, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedJacobiNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        int code = IterationNative.double_mdp_unbounded_jacobi(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedGaussseidelNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);

        int code = IterationNative.double_mdp_unbounded_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedCumulativeJacobiNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance, Value cumul) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = IterationNative.double_mdp_unbounded_cumulative_jacobi(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedCumulativeGaussseidelNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance, Value cumul) throws EPMCException {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = IterationNative.double_mdp_unbounded_cumulative_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }
    
    private static void mdpBoundedCumulativeNative(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, Value cumul) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = IterationNative.double_mdp_bounded_cumulative(bound, numStates, stateBounds,
                nondetBounds, targets, weights, min ? 1 : 0, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpBoundedCumulativeDiscountedNative(int bound,
            ValueReal discount, GraphExplicitSparseAlternate graph, boolean min,
            Value values, Value cumul) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        double discountDouble = discount.getDouble();

        int code = IterationNative.double_mdp_bounded_cumulative_discounted(bound, discountDouble, numStates, stateBounds,
                nondetBounds, targets, weights, min ? 1 : 0, valuesMem, cumulMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static ValueAlgebra newValueWeight() {
    	return TypeWeight.get().newValue();
    }
}
