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
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.imdp.lump.Statistics;
import epmc.imdp.messages.MessagesIMDPGraphsolver;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
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

public final class GraphSolverExplicitIMDPReachabilityJavaDouble implements GraphSolverExplicit{
    private final static class IterateProblem {
        private GraphBuilderExplicit builder;
        private ValueArrayAlgebra values;
        private int steps;
        private boolean min;

        private GraphExplicit getInputGraph() {
            return builder.getInputGraph();
        }

        private GraphExplicitSparseAlternate getOutputGraph() {
            return (GraphExplicitSparseAlternate) builder.getOutputGraph();
        }

        private GraphBuilderExplicit getBuilder() {
            return builder;
        }

        ValueArrayAlgebra getValues() {
            return values;
        }

        public void setValues(ValueArrayAlgebra values) {
            this.values = values;
        }
    }

    public final static String IDENTIFIER = "imdp-java-double";	

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
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            if (!ValueInteger.is(bounded.getTime())) {
                return false;
            }
        }
        if (!(objective instanceof GraphSolverObjectiveExplicitBoundedReachability
                || objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)) {
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
            iterResult = bounded(problem);
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

    private ValueArrayAlgebra bounded(IterateProblem problem) {
        int steps = problem.steps;
        boolean min = problem.min;
        int numStates = problem.getOutputGraph().computeNumStates();
        double[] values = ValueContentDoubleArray.getContent(problem.getValues());
        ValueArrayAlgebra nextValueValues = UtilValue.newArray(getTypeRealArray(), numStates);
        double[] nextValues = ValueContentDoubleArray.getContent(nextValueValues);
        int[] stateBounds = problem.getOutputGraph().getStateBoundsJava();
        IteratorJavaDouble iterator = buildIterator(problem.getOutputGraph(), problem);

        for (int step = 0; step < steps; step++) {
            for (int state = 0; state < numStates; state++) {
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double stateOpt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double ndOpt = iterator.nondetStep(nondet);
                    stateOpt = min
                            ? Math.max(stateOpt, ndOpt)
                                    : Math.min(stateOpt, ndOpt);
                }
                nextValues[state] = stateOpt;
            }
            double[] swap = nextValues;
            nextValues = values;
            values = swap;
            iterator.setValues(values);
        }
        return steps % 2 == 0 ? problem.getValues() : nextValueValues;
    }

    private ValueArrayAlgebra unbounded(IterateProblem problem) {
        IterationMethod iterMethod = Options.get().get(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        switch (iterMethod) {
        case JACOBI:
            return unboundedJacobi(problem);
        case GAUSS_SEIDEL:
            return unboundedGaussSeidel(problem);
        default:
            assert false;
            return null;
        }
    }

    private ValueArrayAlgebra unboundedJacobi(IterateProblem problem) {
        StopWatch time = new StopWatch(true);
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_START,
                getIterationMethod());
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        double precision = getTolerance();
        long numIterations = 0;
        double diff = Double.POSITIVE_INFINITY;
        boolean min = problem.min;
        int numStates = problem.getOutputGraph().computeNumStates();
        double[] values = ValueContentDoubleArray.getContent(problem.getValues());
        ValueArrayAlgebra nextValueValues = UtilValue.newArray(getTypeRealArray(), numStates);
        double[] nextValues = ValueContentDoubleArray.getContent(nextValueValues);
        int[] stateBounds = problem.getOutputGraph().getStateBoundsJava();
        IteratorJavaDouble iterator = buildIterator(problem.getOutputGraph(), problem);

        while (diff > precision) {
            diff = 0;
            for (int state = 0; state < numStates; state++) {
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double stateOpt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double ndOpt = iterator.nondetStep(nondet);
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
        return numIterations % 2 == 0 ? problem.getValues() : nextValueValues;
    }

    private ValueArrayAlgebra unboundedGaussSeidel(IterateProblem problem) {
        getLog().send(MessagesIMDPGraphsolver.IMDP_GRAPHSOLVER_ITER_START,
                getIterationMethod());
        StopWatch time = new StopWatch(true);
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        double precision = getTolerance();
        long numIterations = 0;
        double diff = Double.POSITIVE_INFINITY;
        boolean min = problem.min;
        int numStates = problem.getOutputGraph().computeNumStates();
        double[] values = ValueContentDoubleArray.getContent(problem.getValues());
        int[] stateBounds = problem.getOutputGraph().getStateBoundsJava();
        IteratorJavaDouble iterator = buildIterator(problem.getOutputGraph(), problem);

        while (diff > precision) {
            diff = 0;
            for (int state = 0; state < numStates; state++) {
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double stateOpt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double ndOpt = iterator.nondetStep(nondet);
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
        return problem.getValues();
    }

    private static IteratorJavaDouble buildIterator(GraphExplicitSparseAlternate graph, IterateProblem problem) {
        int numStates = graph.computeNumStates();
        boolean min = problem.min;
        double[] values = ValueContentDoubleArray.getContent(problem.getValues());
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
                .setValues(values)
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

        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            builder.addSink(bounded.getTarget());
            result.steps = ValueInteger.as(bounded.getTime()).getInt();
            result.min = bounded.isMin();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability unbounded = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            builder.addSink(unbounded.getTarget());
            result.steps = Integer.MAX_VALUE;
            result.min = unbounded.isMin();
        }
        builder.build();
        result.builder = builder;
        ValueArrayAlgebra values = UtilValue.newArray(getTypeRealArray(), builder.getOutputGraph().computeNumStates());
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            int numStates = objective.getGraph().computeNumStates();
            BitSet target = bounded.getTarget();
            for (int inputState = 0; inputState < numStates; inputState++) {
                int outputState = builder.inputToOutputNode(inputState);
                values.set(target.get(inputState) ? 1 : 0, outputState);
            }
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability unbounded = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            int numStates = objective.getGraph().computeNumStates();
            BitSet target = unbounded.getTarget();
            for (int inputState = 0; inputState < numStates; inputState++) {
                int outputState = builder.inputToOutputNode(inputState);
                values.set(target.get(inputState) ? 1 : 0, outputState);
            }
        }

        result.setValues(values);
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
