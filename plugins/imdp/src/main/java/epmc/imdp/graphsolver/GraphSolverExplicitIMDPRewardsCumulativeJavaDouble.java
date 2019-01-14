package epmc.imdp.graphsolver;

import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.GraphExplicitSparseAlternate.EdgePropertySparseNondetOnlyNondet;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.imdp.lump.Statistics;
import epmc.imdp.messages.MessagesIMDPGraphsolver;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.util.RunningInfo;
import epmc.value.TypeArray;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueInteger;

import static epmc.util.RunningInfo.startWithInfo;

public final class GraphSolverExplicitIMDPRewardsCumulativeJavaDouble implements GraphSolverExplicit{
    private final static class IterateProblem {
        private GraphBuilderExplicit builder;
        private int steps;
        private boolean min;
        private ValueArrayAlgebra rewards;

        private GraphExplicit getInputGraph() {
            return builder.getInputGraph();
        }

        private GraphExplicitSparseAlternate getOutputGraph() {
            return (GraphExplicitSparseAlternate) builder.getOutputGraph();
        }

        private void setRewards(ValueArrayAlgebra rewards) {
            this.rewards = rewards;
        }

        private ValueArrayAlgebra getRewards() {
            return rewards;
        }

        private GraphBuilderExplicit getBuilder() {
            return builder;
        }
    }

    public final static String IDENTIFIER = "imdp-reward-java-double";	

    private GraphSolverObjectiveExplicit objective;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setGraphSolverObjective(GraphSolverObjectiveExplicit objective) {
        this.objective = objective;
    }

    @Override
    public boolean canHandle() {
        GraphExplicit graph = objective.getGraph();
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsIMDP.isIMDP(semantics)) {
            return false;
        }
        EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        if (!TypeDouble.is(TypeInterval.as(weight.getType()).getEntryType())) {
            return false;
        }
        if (!(objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        StopWatch totalTime = new StopWatch(true);
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_SOLVE_START);
        IterateProblem problem = computeProblem();
        ValueArrayAlgebra iterResult;
        if (problem.steps == Integer.MAX_VALUE) {
            iterResult = unbounded(problem);
        } else {
            iterResult = null;
            //			iterResult = bounded(problem);
        }
        GraphBuilderExplicit builder = problem.getBuilder();
        int numStates = problem.getInputGraph().computeNumStates();
        Value entry = getTypeReal().newValue();
        ValueArray result = UtilValue.newArray(getTypeRealArray(), numStates);
        for (int inputState = 0; inputState < numStates; inputState++) {
            int outputState = builder.inputToOutputNode(inputState);
            iterResult.get(entry, outputState);
            result.set(entry, inputState);
        }
        objective.setResult(result);
        
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_SOLVE_DONE,
                totalTime.getTimeSeconds());
    }

    private ValueArrayAlgebra unbounded(IterateProblem problem) {
        IterationMethod iterMethod = Options.get().get(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        switch (iterMethod) {
        case JACOBI:
            return startWithInfo(info -> unboundedJacobi(info, problem));
        case GAUSS_SEIDEL:
            return startWithInfo(info -> unboundedGaussSeidel(info, problem));
        default:
            assert false;
            return null;
        }
    }

    private ValueArrayAlgebra unboundedJacobi(RunningInfo runningInfo, IterateProblem problem) {
        StopWatch time = new StopWatch(true);
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_START,
                getIterationMethod());
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        InfoSender info = new InfoSender();
        info.setLog(getLog());
        runningInfo.setInformationSender(info);
        runningInfo.setSleepTime(Options.get().getLong(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY));
        double precision = getTolerance();
        long numIterations = 0;
        double diff = Double.POSITIVE_INFINITY;
        boolean min = problem.min;
        int numStates = problem.getOutputGraph().computeNumStates();
        ValueArrayAlgebra valueValues = UtilValue.newArray(getTypeRealArray(), numStates);
        double[] values = ValueContentDoubleArray.getContent(valueValues);
        ValueArrayAlgebra nextValueValues = UtilValue.newArray(getTypeRealArray(), numStates);
        double[] nextValues = ValueContentDoubleArray.getContent(nextValueValues);
        int[] stateBounds = problem.getOutputGraph().getStateBoundsJava();
        IteratorJavaDouble iterator = buildIterator(problem.getOutputGraph(), problem);
        iterator.setValues(values);
        double[] rewards = ValueContentDoubleArray.getContent(problem.getRewards());
        while (diff > precision) {
            info.setNumIterations(numIterations);
            info.setTimePassed(time.getTime());
            info.setDifference(diff);
            info.setNumOpt(iterator.getNumOptSteps());
            info.setNumSort(iterator.getTimesSorted());
            diff = 0;
            for (int state = 0; state < numStates; state++) {
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double stateOpt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double reward = rewards[nondet];
                    double ndOpt = iterator.nondetStep(nondet) + reward;
                    stateOpt = min
                            ? Math.min(stateOpt, ndOpt)
                                    : Math.max(stateOpt, ndOpt);
                }
                diff = Math.max(diff, diffOp.diff(values[state], stateOpt));
                nextValues[state] = stateOpt;
            }
            double[] swap = nextValues;
            nextValues = values;
            values = swap;
            iterator.setValues(values);
            numIterations++;
        }
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_DONE,
                getIterationMethod(),
                time.getTimeSeconds(),
                numIterations,
                iterator.getNumOptSteps(),
                iterator.getTimesSorted());
        return numIterations % 2 == 0 ? valueValues : nextValueValues;
    }

    private ValueArrayAlgebra unboundedGaussSeidel(RunningInfo runningInfo, IterateProblem problem) {
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_START,
                getIterationMethod());
        StopWatch time = new StopWatch(true);
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        InfoSender info = new InfoSender();
        info.setLog(getLog());
        runningInfo.setInformationSender(info);
        runningInfo.setSleepTime(Options.get().getLong(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY));
        double precision = getTolerance();
        long numIterations = 0;
        double diff = Double.POSITIVE_INFINITY;
        boolean min = problem.min;
        int numStates = problem.getOutputGraph().computeNumStates();
        ValueArrayAlgebra valueValues = UtilValue.newArray(getTypeRealArray(), numStates);
        double[] values = ValueContentDoubleArray.getContent(valueValues);
        int[] stateBounds = problem.getOutputGraph().getStateBoundsJava();
        IteratorJavaDouble iterator = buildIterator(problem.getOutputGraph(), problem);
        iterator.setValues(values);
        double[] rewards = ValueContentDoubleArray.getContent(problem.getRewards());
        while (diff > precision) {
            info.setNumIterations(numIterations);
            info.setTimePassed(time.getTime());
            info.setDifference(diff);
            info.setNumOpt(iterator.getNumOptSteps());
            info.setNumSort(iterator.getTimesSorted());
            diff = 0.0;
            for (int state = 0; state < numStates; state++) {
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double stateOpt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double reward = rewards[nondet];
                    double ndOpt = iterator.nondetStep(nondet) + reward;
                    stateOpt = min
                            ? Math.min(stateOpt, ndOpt)
                                    : Math.max(stateOpt, ndOpt);
                }
                diff = Math.max(diff, diffOp.diff(values[state], stateOpt));
                values[state] = stateOpt;
            }
            numIterations++;
        }
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_DONE,
                getIterationMethod(),
                time.getTimeSeconds(),
                numIterations,
                iterator.getNumOptSteps(),
                iterator.getTimesSorted());
        return valueValues;
    }

    private static IteratorJavaDouble buildIterator(GraphExplicitSparseAlternate graph, IterateProblem problem) {
        int numStates = graph.computeNumStates();
        boolean min = problem.min;
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] successors = graph.getTargetsJava();
        EdgePropertySparseNondetOnlyNondet weightProp = (EdgePropertySparseNondetOnlyNondet) graph.getEdgeProperty(CommonProperties.WEIGHT);
        double[] weights = ValueContentDoubleArray.getContent(weightProp.getContent());
        IteratorJavaDouble iterator = new IteratorJavaDouble.Builder()
                .setNumStates(numStates)
                .setStateBounds(stateBounds)
                .setMin(min)
                .setNondetBounds(nondetBounds)
                .setSuccessors(successors)
                .setWeights(weights)
                .build();
        return iterator;
    }

    private IterateProblem computeProblem() {	
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_BUILD_ITER_START);
        StopWatch buildTime = new StopWatch(true);
        IterateProblem result = new IterateProblem();
        GraphBuilderExplicit builder = new GraphBuilderExplicit();
        GraphExplicit original = objective.getGraph();
        builder.setInputGraph(original);
        builder.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        builder.addDerivedNodeProperty(CommonProperties.STATE);
        builder.setReorder();

        if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative) {
            GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            builder.addSink(bounded.getTarget());
            result.steps = ValueInteger.as(bounded.getTime()).getInt();
            result.min = bounded.isMin();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            GraphSolverObjectiveExplicitUnboundedCumulative unbounded = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
            builder.addSinks(unbounded.getSinks());
            result.steps = Integer.MAX_VALUE;
            result.min = unbounded.isMin();
        }
        builder.build();
        result.builder = builder;
        ValueArrayAlgebra rewards = null;
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            GraphSolverObjectiveExplicitUnboundedCumulative unbounded = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
            rewards = unbounded.getStateRewards();
        }

        result.setRewards(rewards);
        UtilIMDPGraphSolver.normalise(result.getOutputGraph());
        Statistics statistics = new Statistics(result.getOutputGraph());
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_BUILD_ITER_DONE,
                buildTime.getTimeSeconds(),
                statistics.getNumStates(),
                statistics.getNumNondet(),
                statistics.getNumFanout());
        return result;
    }

    private IterationMethod getIterationMethod() {
        return Options.get().get(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
    }

    private double getTolerance() {
        return Options.get().getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE) * 4;
    }

    private TypeArray getTypeRealArray() {
        return getTypeReal().getTypeArray();
    }

    private TypeReal getTypeReal() {
        return TypeReal.get();
    }

    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
