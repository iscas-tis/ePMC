package epmc.param.graphsolver;

import java.util.ArrayList;

import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsDTMC;
import epmc.graph.explicit.EdgeProperty;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitSteadyState;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.algorithm.MutableGraphBuilder;
import epmc.param.algorithm.NodeEliminator;
import epmc.param.graph.MutableEdgeProperty;
import epmc.param.graph.MutableGraph;
import epmc.param.graph.MutableNodeProperty;
import epmc.param.graphsolver.eliminationorder.EliminationOrder;
import epmc.param.options.OptionsParam;
import epmc.param.value.TypeStatistics;
import epmc.util.BitSet;
import epmc.util.RunningInfo;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueObject;

public final class GraphSolverEliminator implements GraphSolverExplicit  {
    public final static String IDENTIFIER = "graph-solver-eliminator";
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
        if (!(objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)
                && !(objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative)
                && !(objective instanceof GraphSolverObjectiveExplicitSteadyState)) {
            return false;
        }
        Semantics semantics = ValueObject.as(objective.getGraph().getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsDTMC.isDTMC(semantics) && !SemanticsCTMC.isCTMC(semantics)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        getLog().send(MessagesParamGraphSolver.PARAM_GRAPH_ELIMINATOR_START);
        StopWatch watch = new StopWatch(true);
        MutableGraph graph = buildMutableGraph();
        EliminationOrder order = computeEliminationOrder(graph);
        RunningInfo.startWithInfoVoid(info
                -> eliminateNodes(graph, order, info)); 
        ValueArrayAlgebra result = collectResults(graph);
        objective.setResult(result);
        getLog().send(MessagesParamGraphSolver.PARAM_GRAPH_ELIMINATOR_DONE,
                watch.getTimeSeconds());
        TypeStatistics.sendStatistics(TypeWeightTransition.get());
    }

    private EliminationOrder computeEliminationOrder(MutableGraph graph) {
        assert graph != null;
        EliminationOrder.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_ELIMINATION_ORDER);
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability reachObjective = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            BitSet target = reachObjective.getTarget();            
            builder.setTarget(target);
        }
        EliminationOrder order = builder
                .setGraph(graph)
                .build();
        return order;
    }

    private MutableGraph buildMutableGraph() {
        MutableGraph result = null;
        getLog().send(MessagesParamGraphSolver.PARAM_BUILD_MUTABLE_GRAPH_START);
        StopWatch watch = new StopWatch(true);
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            result = buildMutableGraphUnboundedReachability();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            result = buildMutableGraphUnboundedCumulative();
        } else if (objective instanceof GraphSolverObjectiveExplicitSteadyState) {
            result = buildMutableGraphSteadyState();
        } else {
            assert false;
            return null;
        }
        getLog().send(MessagesParamGraphSolver.PARAM_BUILD_MUTABLE_GRAPH_DONE,
                watch.getTimeSeconds());
        return result;
    }

    private MutableGraph buildMutableGraphUnboundedReachability() {
        GraphSolverObjectiveExplicitUnboundedReachability reachObjective = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        BitSet zero = reachObjective.getZeroSet();
        BitSet target = reachObjective.getTarget();
        int j = -1;
        for (int i = target.length(); i >= 0; i--) {
            if (target.get(i)) {
                j = i;
                break;
            }
        }
        int firstTarget = j;
        MutableGraphBuilder.NodeMap map = (int node) -> {
            if (target.get(node)) {
                return firstTarget;
            }
            return node;
        };
        MutableGraph graph = new MutableGraphBuilder()
                .setOriginalGraph(reachObjective.getGraph())
                .setSinks(target)
                .setSuccessorMap(map)
                .build();
        return graph;
    }

    private MutableGraph buildMutableGraphUnboundedCumulative() {
        GraphSolverObjectiveExplicitUnboundedCumulative cumulativeObjective = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        BitSet sinks = cumulativeObjective.getSinks().get(0);
        int j = -1;
        for (int i = sinks.length(); i >= 0; i--) {
            if (sinks.get(i)) {
                j = i;
                break;
            }
        }
        int firstTarget = j;
        MutableGraphBuilder.NodeMap map = (int node) -> {
            if (sinks.get(node)) {
                return firstTarget;
            }
            return node;
        };
        MutableGraph graph = new MutableGraphBuilder()
                .setOriginalGraph(cumulativeObjective.getGraph())
                .setSinks(sinks)
                .setSuccessorMap(map)
                .addRewardArray(cumulativeObjective.getStateRewards())
                .build();
        return graph;
    }

    private MutableGraph buildMutableGraphSteadyState() {
        GraphSolverObjectiveExplicitSteadyState steadyStateObjective = (GraphSolverObjectiveExplicitSteadyState) objective;
        MutableGraph graph = new MutableGraphBuilder()
                .setOriginalGraph(steadyStateObjective.getGraph())
                .setAddTime(true)
                .addRewardArray(steadyStateObjective.getStateRewards())
                .build();
        return graph;
    }

    private void eliminateNodes(MutableGraph graph, EliminationOrder order, RunningInfo running) {
        getLog().send(MessagesParamGraphSolver.PARAM_ELIMINATION_START,
                graph.getNumNodes(), graph.getTotalNumTransitions());
        StopWatch watch = new StopWatch(true);
        int numNodes = graph.getNumNodes();
        EliminationInformationSender info = buildInfo(graph, running);
        BitSet computeFor = objective.getComputeFor();
        NodeEliminator eliminator = buildEliminator(graph);
        int todo = graph.getNumNodes();
        int maxTotalNumTransitions = 0;
        while (order.hasNodes()) {
            int node = order.nextNode();
            info.setStatesDone(numNodes - todo);
            int totalNumTransitions = graph.getTotalNumTransitions();
            info.setNumTransitions(totalNumTransitions);
            maxTotalNumTransitions = Math.max(maxTotalNumTransitions, totalNumTransitions);
            eliminator.eliminate(node);
            if (computeFor != null && !computeFor.get(node)
                    && graph.getNumPredecessors(node) == 0) {
                eliminator.removeNode(node);
            }
            todo--;
        }
        getLog().send(MessagesParamGraphSolver.PARAM_ELIMINATION_DONE,
                graph.getNumNodesUsed(), graph.getTotalNumTransitions(),
                maxTotalNumTransitions, watch.getTimeSeconds());
    }

    private EliminationInformationSender buildInfo(MutableGraph graph, RunningInfo running) {
        int numNodes = graph.getNumNodes();
        EliminationInformationSender info = new EliminationInformationSender(numNodes);
        long sleepTime = Options.get().getLong(OptionsGraphsolver.GRAPHSOLVER_UPDATE_DELAY);
        running.setSleepTime(sleepTime);
        running.setInformationSender(info);
        return info;
    }

    private NodeEliminator buildEliminator(MutableGraph graph) {
        NodeEliminator.Builder eliminatorBuilder = new NodeEliminator.Builder();
        eliminatorBuilder.setGraph(graph)
        .setWeigherMethod(Options.get().get(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD));
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            GraphSolverObjectiveExplicitUnboundedCumulative objC = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
            ArrayList<Object> rews = new ArrayList<Object>();
            rews.add(objC.getStateRewards());
            eliminatorBuilder.setRewards(rews);
        } else if (objective instanceof GraphSolverObjectiveExplicitSteadyState) {
            GraphSolverObjectiveExplicitSteadyState objS = (GraphSolverObjectiveExplicitSteadyState) objective;
            ArrayList<Object> rews = new ArrayList<Object>();
            rews.add(objS.getStateRewards());
            rews.add(MutableGraphBuilder.PropertyNames.TIME);
            eliminatorBuilder.setRewards(rews);            
        }
        return eliminatorBuilder.build();
    }

    private ValueArrayAlgebra collectResults(MutableGraph graph) {
        ValueArrayAlgebra result = null;
        getLog().send(MessagesParamGraphSolver.PARAM_COLLECT_RESULTS_START);
        StopWatch watch = new StopWatch(true);
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            result = collectResultsUnboundedReachability(graph);
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            result = collectResultsUnboundedCumulative(graph);
        } else if (objective instanceof GraphSolverObjectiveExplicitSteadyState) {
            result = collectResultsSteadyState(graph);
        } else {
            assert false;
            return null;
        }
        getLog().send(MessagesParamGraphSolver.PARAM_COLLECT_RESULTS_DONE,
                watch.getTimeSeconds());
        return result;
    }
    
    private ValueArrayAlgebra collectResultsUnboundedReachability(MutableGraph graph) {
        BitSet computeFor = objective.getComputeFor();
        GraphSolverObjectiveExplicitUnboundedReachability reachObjective = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        BitSet target = reachObjective.getTarget();
        int numNodes = graph.getNumNodes();
        ValueArrayAlgebra result = TypeWeight.get().getTypeArray().newValue();
        result.setSize(numNodes);
        ValueAlgebra sum = TypeWeightTransition.get().newValue();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, 
                TypeWeightTransition.get(), TypeWeightTransition.get());
        MutableEdgeProperty probability = (MutableEdgeProperty) graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            if (computeFor != null && !computeFor.get(node)) {
                continue;
            }
            for (int succNr = 0; succNr <  graph.getNumSuccessors(node); succNr++) {
                int succ = graph.getSuccessorNode(node, succNr);
                if (target.get(succ)) {
                    add.apply(sum, sum, probability.get(node, succNr));
                }
            }
            result.set(sum, node);
            sum.set(0);
        }
        return result;
    }
    
    private ValueArrayAlgebra collectResultsUnboundedCumulative(MutableGraph graph) {
        BitSet computeFor = objective.getComputeFor();
        GraphSolverObjectiveExplicitUnboundedCumulative reachObjective = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        int numNodes = graph.getNumNodes();
        ValueArrayAlgebra result = TypeWeight.get().getTypeArray().newValue();
        result.setSize(numNodes);
        ValueAlgebra sum = TypeWeightTransition.get().newValue();
        MutableNodeProperty reward = (MutableNodeProperty) graph.getNodeProperty(reachObjective.getStateRewards());
        for (int node = 0; node < graph.getNumNodes(); node++) {
            if (computeFor != null && !computeFor.get(node)) {
                continue;
            }
            result.set(reward.get(node), node);
            sum.set(0);
        }
        return result;
    }

    private ValueArrayAlgebra collectResultsSteadyState(MutableGraph graph) {
        BitSet computeFor = objective.getComputeFor();
        GraphSolverObjectiveExplicitSteadyState steadyStateObjective = (GraphSolverObjectiveExplicitSteadyState) objective;
        int numNodes = graph.getNumNodes();
        ValueArrayAlgebra result = TypeWeight.get().getTypeArray().newValue();
        result.setSize(numNodes);
        EdgeProperty weights = graph.getEdgeProperty(CommonProperties.WEIGHT);
        MutableNodeProperty nominator = (MutableNodeProperty) graph.getNodeProperty(steadyStateObjective.getStateRewards());
        MutableNodeProperty denominator = (MutableNodeProperty) graph.getNodeProperty(MutableGraphBuilder.PropertyNames.TIME);
        ValueAlgebra componentValue = TypeWeightTransition.get().newValue();
        ValueAlgebra weighted = TypeWeightTransition.get().newValue();
        ValueAlgebra sum = TypeWeightTransition.get().newValue();
        sum.set(0);
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        for (int node = 0; node < graph.getNumNodes(); node++) {
            if (computeFor != null && !computeFor.get(node)) {
                continue;
            }
            int numSuccessors = graph.getNumSuccessors(node);
            if (numSuccessors == 1 && graph.getSuccessorNode(node, 0) == node) {
                collectSCCValue(componentValue, nominator, denominator, node);
                result.set(componentValue, node);
            } else {
                sum.set(0);
                for (int succNr = 0; succNr < numSuccessors; succNr++) {
                    int succ = graph.getSuccessorNode(node, succNr);
                    collectSCCValue(componentValue, nominator, denominator, succ);  
                    Value prob = weights.get(node, succNr);
                    multiply.apply(weighted, componentValue, prob);
                    add.apply(sum, sum, weighted);
                }
                result.set(sum, node);
            }
        }
        return result;
    }
    
    private void collectSCCValue(ValueAlgebra result, MutableNodeProperty nominator, MutableNodeProperty denominator, int node) {
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        divide.apply(result, nominator.get(node), denominator.get(node));
    }

    static Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
