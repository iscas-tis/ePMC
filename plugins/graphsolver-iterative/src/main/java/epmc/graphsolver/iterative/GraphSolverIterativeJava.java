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

import epmc.algorithms.FoxGlynn;
import epmc.error.EPMCException;
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
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import epmc.value.ValueReal;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The implementations provided here should work for any representation of
 * reals, not just doubles. Due to their generality, they are not that
 * efficient. Their purpose is mainly to be used with representations of
 * reals the computations of which are not directly implemented in hardware
 * (e.g. mpfr numbers). Here, the additional overhead does not matter that
 * much: the majority of time is spent in rather slow computations involving
 * real values, such that the additional time added from generality does not
 * contribute too much to the overall runtime.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphSolverIterativeJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-java";
    
    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private int numIterations;
    private ValueReal lambda;
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
    			&& !(objective instanceof GraphSolverObjectiveExplicitBoundedReachability)
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
        Semantics semantics = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            if (SemanticsContinuousTime.isContinuousTime(semantics)) {
                ctBoundedReachability();
            } else {
                dtBoundedReachability();
            }
        } else if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative) {
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
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBoundedReachability);
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
        } else if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
        	sinks = new ArrayList<>();
        	GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
        	if (bounded.getZeroSet() != null) {
        		sinks.add(bounded.getZeroSet());
        	}
        	sinks.add(bounded.getTarget());
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
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            this.lambda = TypeReal.get().newValue();
            GraphSolverObjectiveExplicitBoundedReachability objectiveBounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            Value time = objectiveBounded.getTime();
            this.lambda.multiply(time, unifRate);        	
        }
        BitSet targets = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
        	GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        	targets = objectiveUnboundedReachability.getTarget();
        } else if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
        	GraphSolverObjectiveExplicitBoundedReachability objectiveBoundedReachability = (GraphSolverObjectiveExplicitBoundedReachability) objective;
        	targets = objectiveBoundedReachability.getTarget();
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
        if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
        	dtmcUnboundedJacobiJava(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision);
        } else if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            dtmcUnboundedGaussseidelJava(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpUnboundedJacobiJava(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpUnboundedGaussseidelJava(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision);
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
        if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
        	dtmcUnboundedCumulativeJacobiJava(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            dtmcUnboundedCumulativeGaussseidelJava(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpUnboundedCumulativeJacobiJava(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpUnboundedCumulativeGaussseidelJava(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards);
        } else {
            assert false;
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations,
                timer.getTimeSeconds());
    }
    
    private void dtBoundedReachability() throws EPMCException {
        assert iterGraph != null;
        GraphSolverObjectiveExplicitBoundedReachability objectiveBoundedReachability = (GraphSolverObjectiveExplicitBoundedReachability) objective;
        ValueInteger time = ValueInteger.asInteger(objectiveBoundedReachability.getTime());
        assert time.getInt() >= 0;
        numIterations = time.getInt();
        boolean min = objectiveBoundedReachability.isMin();
        if (isSparseMarkovJava(iterGraph)) {
            dtmcBoundedJava(time.getInt(), asSparseMarkov(iterGraph), inputValues);
        } else if (isSparseMDPJava(iterGraph)) {
            mdpBoundedJava(time.getInt(), asSparseNondet(iterGraph), min, inputValues);            
        } else {
            assert false : isSparseMarkov(iterGraph) + " " + isSparseNondet(iterGraph);
        }
    }

    private void boundedCumulative() throws EPMCException {
        assert iterGraph != null;
        GraphSolverObjectiveExplicitBoundedCumulative objectiveBoundedCumulative = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
        ValueInteger time = ValueInteger.asInteger(objectiveBoundedCumulative.getTime());
        assert time.getInt() >= 0;
        numIterations = time.getInt();
        boolean min = objectiveBoundedCumulative.isMin();
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        if (isSparseMarkovJava(iterGraph)) {
            dtmcBoundedCumulativeJava(time.getInt(), asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (isSparseMDPJava(iterGraph)) {
            mdpBoundedCumulativeJava(time.getInt(), asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
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
        if (isSparseMarkovJava(iterGraph)) {
            dtmcBoundedCumulativeDiscountedJava(time.getInt(), discount, asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (isSparseMDPJava(iterGraph)) {
            mdpBoundedCumulativeDiscountedJava(time.getInt(), discount, asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
        } else {
            assert false;
        }
    }

    private void ctBoundedReachability() throws EPMCException {
        assert iterGraph != null : "iterGraph == null";
        assert lambda != null : "lambda == null";
        assert ValueReal.isReal(lambda) : lambda;
        assert !lambda.isPosInf() : lambda;
        Options options = Options.get();
        ValueReal precision = UtilValue.newValue(TypeReal.get(), options.getString(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE));
        FoxGlynn foxGlynn = new FoxGlynn(lambda, precision);
        if (isSparseMarkovJava(iterGraph)) {
            ctmcBoundedJava(asSparseMarkov(iterGraph), inputValues, foxGlynn);
        } else {
            assert false;
        }
    }

    /* auxiliary methods */
    
    private static void compDiff(double[] distance, ValueAlgebra previous,
            Value current, IterationStopCriterion stopCriterion) throws EPMCException {
        if (stopCriterion == null) {
            return;
        }
        double thisDistance = previous.distance(current);
        if (stopCriterion == IterationStopCriterion.RELATIVE) {
            double presNorm = previous.norm();
            if (presNorm != 0.0) {
                thisDistance /= presNorm;
            }
        }
        distance[0] = Math.max(distance[0], thisDistance);
    }
    
    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }
    
    private static boolean isSparseMarkov(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparse;
    }
    
    private static boolean isSparseMDPJava(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        return true;
    }
    
    private static boolean isSparseMarkovJava(GraphExplicit graph) {
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
    
    /* implementation of iteration algorithms */    
    
    private void dtmcUnboundedJacobiJava(GraphExplicitSparse graph,
            ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance) throws EPMCException {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueArray swap;
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        double[] distance = new double[1];
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                presValues.get(presStateProb, state);
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }

    private void dtmcUnboundedGaussseidelJava(GraphExplicitSparse graph,
            ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    values.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }

    private void dtmcUnboundedCumulativeJacobiJava(GraphExplicitSparse graph,
            ValueArray values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul) throws EPMCException {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueArray swap;
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                presValues.get(presStateProb, state);
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }

    private void dtmcUnboundedCumulativeGaussseidelJava(GraphExplicitSparse graph,
    		ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    values.get(succStateProb, succState);
                    weighted.multiply(weight, succStateProb);
                    nextStateProb.add(nextStateProb, weighted);
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }

    private void ctmcBoundedJava(GraphExplicitSparse graph,
            ValueArray values, FoxGlynn foxGlynn) throws EPMCException {
    	ValueArrayAlgebra fg = foxGlynn.getArray();
        Value fgWeight = foxGlynn.getTypeReal().newValue();
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = UtilValue.newArray(values.getType(), numStates);
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra value = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        Value zero = foxGlynn.getTypeReal().getZero();
        for (int i = foxGlynn.getRight() - foxGlynn.getLeft(); i >= 0; i--) {
            fg.get(fgWeight, i);
            for (int state = 0; state < numStates; state++) {
                values.get(value, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.multiply(fgWeight, value);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(weight, succStateProb);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        for (int i = foxGlynn.getLeft() - 1; i >= 0; i--) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;            
        }
        values.set(presValues);
    }

    private static void dtmcBoundedJava(int bound,
            GraphExplicitSparse graph, ValueArrayAlgebra values)
            		throws EPMCException {
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        values.set(presValues);
    }

    private void dtmcBoundedCumulativeJava(int bound,
            GraphExplicitSparse graph, ValueArray values, ValueArray cumul)
                    throws EPMCException {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        Value presStateProb = newValueWeight();
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                presValues.get(presStateProb, state);
                nextValues.set(nextStateProb, state);
            }
            ValueArray swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        values.set(presValues);
    }

    private void dtmcBoundedCumulativeDiscountedJava(int bound,
            Value discount, GraphExplicitSparse graph, ValueArray values, ValueArray cumul)
                    throws EPMCException {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    weighted.multiply(weighted, discount);
                    nextStateProb.add(nextStateProb, weighted);
                }
                presValues.get(presStateProb, state);
                nextValues.set(nextStateProb, state);
            }
            ValueArray swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        values.set(presValues);
    }
    
    private void mdpUnboundedJacobiJava(
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, IterationStopCriterion stopCriterion,
            double tolerance) throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(zero);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }

    private void mdpUnboundedGaussseidelJava(
            GraphExplicitSparseAlternate graph, boolean min, ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance)
                    throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(zero);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        values.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }
        
    private static void mdpUnboundedCumulativeJacobiJava(
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, IterationStopCriterion stopCriterion,
            double tolerance, ValueArrayAlgebra cumul) throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    cumul.get(choiceNextStateProb, nondetNr);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }

    private void mdpUnboundedCumulativeGaussseidelJava(
            GraphExplicitSparseAlternate graph, boolean min, ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul)
                    throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    cumul.get(choiceNextStateProb, nondetNr);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        values.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }
    
    private void mdpBoundedJava(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values) throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(zero);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArray swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        }
        values.set(presValues);
    }

    private void mdpBoundedCumulativeJava(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, ValueArray cumul) throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    cumul.get(choiceNextStateProb, nondetNr);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        }
        values.set(presValues);
    }

    private void mdpBoundedCumulativeDiscountedJava(int bound,
            Value discount, GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, ValueArray cumul) throws EPMCException {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    cumul.get(choiceNextStateProb, nondetNr);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        weighted.multiply(weighted, discount);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (min) {
                        nextStateProb.min(nextStateProb, choiceNextStateProb);
                    } else {
                        nextStateProb.max(nextStateProb, choiceNextStateProb);
                    }
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        }
        values.set(presValues);
    }

    private static ValueAlgebra newValueWeight() {
    	return TypeWeight.get().newValue();
    }
}
