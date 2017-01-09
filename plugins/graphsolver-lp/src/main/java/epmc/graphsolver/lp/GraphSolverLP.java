package epmc.graphsolver.lp;

import java.util.Arrays;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueContentIntArray;

// TODO make sure that this thing still works and write some JUnit tests

/**
 * @author Li Yong
 */
public final class GraphSolverLP implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-lp";
    
    private GraphExplicit graph;
//    private boolean min;
    private ValueArray values;
    private int numConstrints;
    private int numVariables;
    private Value scheduler;
    private BitSet targets;
	private GraphSolverObjectiveExplicit objective;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setGraphSolverObjective(
            GraphSolverObjectiveExplicit obj) {
        this.objective = obj;
//        this.min = configuration.isMin();            /* do not know what is this*/
        GraphSolverObjectiveExplicitUnboundedReachability objective = (GraphSolverObjectiveExplicitUnboundedReachability) obj;
        this.graph = objective.getGraph();
        this.targets = objective.getTarget();
//        this.scheduler = configuration.getInputScheduler();
    }

    @Override
    public boolean canHandle() {
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        GraphExplicit graph = objective.getGraph();

        // TODO check
        if ((SemanticsDTMC.isDTMC(semantics) || SemanticsMDP.isMDP(semantics)) &&
                (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)) {
            return true;
        }
        return false;
    }
    
    /** according to canSolve, we only accept unbounded reachability 
     * of DTMC and MDP , do not worry about other things */
    @Override
    public void solve() throws EPMCException {
        // TODO Auto-generated method stub
        assert this.targets != null;
        assert this.graph != null;
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (SemanticsDTMC.isDTMC(semantics)) {
            values = solveMCLP(graph, targets);
        } else {
            values = solveMDPLP(graph, targets);
            /* here we can get a scheduler */
        }
        objective.setResult(values);
    }
    
    /** will implement if it is necessary in future */
    private void abstractScheduler() {
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        Arrays.fill(schedulerJava, -1);
//        for()
    }

    /** computed ZERO states in DTMC, that is, they can not reach target states with positive probability
     * could also be used in MDP, may contain actions */
    private BitSet computeProb0() throws EPMCException {
        graph.computePredecessors();
        BitSet oldPrev = targets.clone();
        BitSet reachSome = targets.clone();
//        BitSet visited = new BitSet();
        System.out.println("Starting to compute Prob0 states ...");
        StopWatch timer = new StopWatch(true);
        while(oldPrev.cardinality() != 0 ) { /* some new predecessors */
            reachSome.or(oldPrev);
            BitSet newPrev = oldPrev.clone();
            for(int node = newPrev.nextSetBit(0); node >= 0 ; node = newPrev.nextSetBit(node + 1)) {
                graph.queryNode(node);
                for(int preNr = 0 ; preNr < graph.getNumPredecessors() ; preNr ++) {
                    int prev = graph.getPredecessorNode(preNr);
                    oldPrev.set(prev);
                }
            }
            oldPrev.andNot(reachSome); /* if there is new predecessors */
        }
//        System.out.println("reachSome: " + reachSome);
        BitSet nodes = UtilBitSet.newBitSetUnbounded();
        nodes.set(0, graph.getNumNodes(), true);
        nodes.andNot(reachSome);
        System.out.println("Done for computing Prob0 states in " + timer.getTimeSeconds() + " secs...");
        return nodes;
    }
    
    /** for MCs, every state is state in MC, no need to check whether it is state */
    private ValueArray solveMCLP(GraphExplicit graph, BitSet acc) 
            throws EPMCException {
        Options options = graph.getOptions();
        Log log = options.get(OptionsMessages.LOG);
        ContextValue contextValue = graph.getContextValue();
        log.send(MessagesGraphSolverLP.PREPARING_MDP_FOR_ITERATION);      
        TypeAlgebra typeWeight = TypeWeight.get(contextValue);
        Value one = typeWeight.getOne();
        Value zero = typeWeight.getZero();
        this.numConstrints = 0;
        this.numVariables = 0;
        /** zero states calculation */
//        System.out.println("buildGraph: " + acc);
//        GraphExporter.export(graph, System.out);
        /** I assume that after configuration, all zero states are not in the graph,
         * apparently, they do not delete those */
        BitSet zeroStates = computeProb0();
//        System.out.println("zero: " + zeroStates);
        /** prepare variables for the LP problem */
        ConstraintSolverConfiguration contextConstraintSolver = new ConstraintSolverConfiguration(graph.getContextValue());
        contextConstraintSolver.requireFeature(Feature.LP);
        ConstraintSolver lpProblem = contextConstraintSolver.newProblem();

        for (int node = 0; node < graph.getNumNodes(); node++) {
            int varIndex = lpProblem.addVariable("x_" + node, typeWeight);
            /** every variable must be in [0, 1] */
            lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.LE, one);
            lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.GE, zero);
            /* every state i in ACC : x_i = 1 */
            numConstrints += 2;
            if(acc.get(node)) {
                lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.EQ, one);
                numConstrints ++;
            }
            /* every state i in ZERO : x_i = 0 */
            if(zeroStates.get(node)) {
                lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.EQ, zero);
                numConstrints ++;            
            }
            
            assert numVariables == node : "not incremental manner";
            ++ numVariables ;
            
        }
        /** input all the constraints , first x_i = 1*/
        BitSet undecided = UtilBitSet.newBitSetUnbounded();
        undecided.set(0, graph.getNumNodes(), true);
        undecided.andNot(acc);
        undecided.andNot(zeroStates);

        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        /** input all the constraints in transition x_i = p1 * x_j1 + ... + pn * x_jn */
        for(int node = undecided.nextSetBit(0); 
                node >= 0 ; 
                node = undecided.nextSetBit(node + 1)) {
            graph.queryNode(node);
            int numSuccessors = graph.getNumSuccessors();
            Value[] row = new Value[numSuccessors + 1];         /** coefficient row */
            int [] varsIndex = new int[numSuccessors + 1];      /** variables   row */
            row[0] = one;
            varsIndex[0] = node;                /* directly use this node index */
            
            for(int i = 1 ; i < row.length ; i ++) {
                row[i] = zero;
            }
            int jIndex = 1;
            int[] visited = new int[graph.getNumNodes()];  /** will be initialized to 0*/
            visited[node] = 1;
            for(int succNr = 0 ; succNr < numSuccessors ; succNr ++) {
                int succ = graph.getSuccessorNode(succNr);
                /** if it is the first time */
                Value tranProb = weightProp.get(succNr);
                ValueAlgebra prob = typeWeight.newValue();
                if (visited[succ] == 0) {
                    prob.subtract(zero, tranProb);
                    row[jIndex] = prob;
                    varsIndex[jIndex] = succ;     /* directly use this node index */
                    jIndex ++;
                    visited[succ] = jIndex;       /* index + 1 */
                }else { /** not first time be successor */
                    int j = visited[succ] - 1;
                    prob.subtract(row[j], tranProb);
                    row[j] = prob;
                }
            }
            boolean canAdd = false;
            for(int r = 0 ; r < jIndex ; r ++) 
                if (!ValueAlgebra.asAlgebra(row[r]).isZero()) { 
                    canAdd = true;
                    break;
                }/* non-zero number */
            /** if input x4 - 0.5x0 - 0.5x5 - 0.5x6- 0.5x0 = 0, then all coefficient will be 0,
             * i.e. 0 = 0, this is quite annoying, so I modify above code to fix this */
            if(canAdd) {
                 
                if(jIndex == row.length) {
                    lpProblem.addConstraint(row, varsIndex, ConstraintType.EQ, zero);
                }else {
                    Value[] rowArr = new Value[jIndex];
                    int[] varArr = new int[jIndex];
                    System.arraycopy(row, 0, rowArr, 0, jIndex);
                    System.arraycopy(varsIndex, 0, varArr, 0, jIndex);
                    lpProblem.addConstraint(rowArr, varArr, ConstraintType.EQ, zero);
                }
                
                numConstrints ++;
            }
            
        }
//        System.out.println("LP problem: \n" + lpProblem.toString());
        /** finally, set objective and direction, min sum x_i for all i */
        StopWatch timer = new StopWatch(true);
        ConstraintSolverResult status = lpProblem.solve();
        assert status == ConstraintSolverResult.SAT : "UNSAT equation system";
        System.out.println("Done for solving LP problem for MC in " + timer.getTimeSeconds() + " secs");
        System.out.println("LP problem: numVars =" + this.numVariables + " , numConstr=" + this.numConstrints);
        ValueArray result = lpProblem.getResultVariablesValuesSingleType();
        lpProblem.close();
//        System.out.println("result: " + result);
        /** do not know whether we need vars, turns out that we do not need it */
        return result;
    }
    
    
    private ValueArray solveMDPLP(GraphExplicit graph, BitSet acc) throws EPMCException {
  
        Options options = graph.getOptions();
        Log log = options.get(OptionsMessages.LOG);
        ContextValue contextValue = graph.getContextValue();
        log.send(MessagesGraphSolverLP.PREPARING_MDP_FOR_ITERATION);      
        TypeAlgebra typeWeight = TypeWeight.get(contextValue);
        Value one = typeWeight.getOne();
        Value zero = typeWeight.getZero();
        
        this.numConstrints = 0;
        this.numVariables = 0;
        /** zero states calculation */
//        System.out.println("buildGraph: " + acc);
//        GraphExporter.export(graph, System.out);
        /** I assume that after configuration, all zero states are not in the graph,
         * apparently, they do not delete those */
        BitSet zeroStates = computeProb0();
        
        /** prepare variables for the LP problem */
        ConstraintSolverConfiguration contextConstraintSolver = new ConstraintSolverConfiguration(graph.getContextValue());
        contextConstraintSolver.requireFeature(Feature.LP);
        ConstraintSolver lpProblem = contextConstraintSolver.newProblem();
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);

        Value[] objRow = new Value[graph.getNumNodes()];
        int[] objVars = new int[graph.getNumNodes()];
        
        for (int node = 0; node < graph.getNumNodes(); node++) {
            int varIndex = lpProblem.addVariable("x_" + node, typeWeight);
            /** every variable must be in [0, 1] */
            lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.LE, one);
            lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.GE, zero);
            /* every state i in ACC : x_i = 1 */
            numConstrints += 2;
            if(acc.get(node)) {
                lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.EQ, one);
                numConstrints ++;
            }
            /* every state i in ZERO : x_i = 0 */
            if(zeroStates.get(node)) {
                lpProblem.addConstraint(new Value[]{one}, new int[] {varIndex}, ConstraintType.EQ, zero);
                numConstrints ++;            
            }
            graph.queryNode(node);
            if(isState.getBoolean()) {
                objRow[node] = one;
            }else {
                objRow[node] = zero;
            }
            objVars[node] = varIndex;
            
            assert numVariables == node : "not incremental manner";
            ++ numVariables ;
        }
        
        /** input all the constraints , first x_i = 1*/
        BitSet undecided = UtilBitSet.newBitSetUnbounded();
        undecided.set(0, graph.getNumNodes(), true);
        undecided.andNot(acc);
        undecided.andNot(zeroStates);
        
        ValueAlgebra minusOne = typeWeight.newValue();
        minusOne.subtract(zero, one);
        
        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        /** input all the constraints in transition x_i = p1 * x_j1 + ... + pn * x_jn */
        for(int node = undecided.nextSetBit(0); 
                node >= 0 ; 
                node = undecided.nextSetBit(node + 1)) {
            graph.queryNode(node);
            int numSuccessors = graph.getNumSuccessors();
            if(isState.getBoolean()) {
                for(int succNr = 0 ; succNr < graph.getNumSuccessors() ; succNr ++) {
                    int succ = graph.getSuccessorNode(succNr);
                    /** if it is the first time */
                    lpProblem.addConstraint(new Value[]{one, minusOne}, new int[]{node, succ}, ConstraintType.GE, zero);
                }
            }else if(numSuccessors > 0){
                
                Value[] row = new Value[numSuccessors + 1];         /** coefficient row */
                int [] varsIndex = new int[numSuccessors + 1];      /** variables   row */
                row[0] = typeWeight.getOne();
                varsIndex[0] = node;                /* directly use this node index */
                
                int jIndex = 1;
                for(int succNr = 0 ; succNr < numSuccessors ; succNr ++) {
                    int succ = graph.getSuccessorNode(succNr);  /* can not occur two times */
                    /** if it is the first time */
                    Value tranProb = weightProp.get(succNr);
                    ValueAlgebra prob = typeWeight.newValue();
                    prob.subtract(zero, tranProb);
                    row[jIndex] = prob;
                    varsIndex[jIndex] = succ;     /* directly use this node index */
                    jIndex ++;
                }
                boolean canAdd = false;
                for(int r = 0 ; r < row.length ; r ++) 
                    if (!ValueAlgebra.asAlgebra(row[r]).isZero()) { 
                        canAdd = true;
                        break;
                    }/* non-zero number */
                /** if input x4 - 0.5x0 - 0.5x5 - 0.5x6- 0.5x0 = 0, then all coefficient will be 0,
                 * i.e. 0 = 0, this is quite annoying, so I modify above code to fix this */
                if(canAdd) {
                    lpProblem.addConstraint(row, varsIndex, ConstraintType.EQ, zero);
                    numConstrints ++;
                }
                
            }
        }
//        System.out.println("LP problem: \n" + lpProblem.toString());
        /** finally, set objective and direction, min sum x_i for all i */
        lpProblem.setDirection(Direction.MIN);
        lpProblem.setObjective(objRow, objVars);
//        System.out.println("LP sytem for MDP: \n" + lpProblem.toString());
        StopWatch timer = new StopWatch(true);
        ConstraintSolverResult status = lpProblem.solve();
        assert status == ConstraintSolverResult.SAT : "UNSAT equation system";
        System.out.println("Done for solving LP problem for MDP in " + timer.getTimeSeconds() + " secs");
        System.out.println("LP problem: numVars =" + this.numVariables + " , numConstr=" + this.numConstrints);
        ValueArray result = lpProblem.getResultVariablesValuesSingleType();
//        System.out.println("result: " + result);
        lpProblem.close();
        /** do not know whether we need vars, turns out that we do not need it */
        return result;
    }
}
