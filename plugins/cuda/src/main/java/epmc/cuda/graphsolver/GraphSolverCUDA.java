package epmc.cuda.graphsolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.BitSet;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.EdgeProperty;
import epmc.graph.GraphExplicit;
import epmc.graph.NodeProperty;
import epmc.graph.Semantics;
import epmc.graphsolver.CommonGraphSolverObjective;
import epmc.graphsolver.GraphSolver;
import epmc.graphsolver.GraphSolverConfiguration;
import epmc.options.Options;
import epmc.value.Value;

public class GraphSolverCUDA implements GraphSolver {
    public final static String IDENTIFIER = "graph-solver-cuda";
    private GraphSolverConfiguration configuration;
    private GraphExplicit graph;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setGraphSolverConfiguration(
            GraphSolverConfiguration configuration) {
        this.configuration = configuration;
        this.graph = configuration.getGraph();
        System.out.println("GraphClass " + graph.getClass());
    }

    @Override
    public boolean canSolve() {
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!semantics.isMDP()) {
            return false;
        }
    	boolean unbounded = this.configuration.getObjective() == CommonGraphSolverObjective.UNBOUNDED_REACHABILITY;
    	boolean bounded = this.configuration.getObjective() == CommonGraphSolverObjective.BOUNDED_REACHABILITY;
    	if (!unbounded && !bounded) {
    		return false;
    	}
        return true;
    }

    @Override
    public void solve() throws EPMCException {
        Options options = graph.getOptions();
        String filename = options.getString("cuda-filename");
        System.out.println("writing " + filename);
        (new File(filename)).mkdir();
        try (PrintStream mdpParameters = new PrintStream(filename + "/MDPparameters.txt");) {
            int numMatrices = countMaxNondetFanout();
            PrintStream[] matrixFiles = new PrintStream[numMatrices];
        	for (int matrixNr = 0; matrixNr < numMatrices; matrixNr++) {
				matrixFiles[matrixNr] = new PrintStream(filename + "/matrices" + (matrixNr + 1) + ".txt");
        	}
            int numStates = countNumStates();
            int maxActionFanout = countMaxActionFanout();
            int[] nodeToStateNumber = computeNodeToStateNumber();
            mdpParameters.print(numStates + " ");
            mdpParameters.print(numMatrices + " ");
            mdpParameters.print(maxActionFanout + " ");
            mdpParameters.print(configuration.getPrecision() + " ");
            printInitialStates(nodeToStateNumber, mdpParameters);
            printTargetStates(nodeToStateNumber, mdpParameters);
            printStepBounds(mdpParameters);
            mdpParameters.print(this.configuration.isMin() ? 0 : 1 + " ");
            mdpParameters.println();
            for (int nondetNr = 0; nondetNr < numMatrices; nondetNr++) {
                printMatrix(nodeToStateNumber, nondetNr, matrixFiles[nondetNr]);
            }
        	for (int matrixNr = 0; matrixNr < numMatrices; matrixNr++) {
        		matrixFiles[matrixNr].close();
        	}
            // TODO Auto-generated method stub
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assert false;
        }
        System.out.println("done " + filename);
    }

    private void printStepBounds(PrintStream out) {
    	assert out != null;
    	boolean unbounded = this.configuration.getObjective() == CommonGraphSolverObjective.UNBOUNDED_REACHABILITY;
    	boolean bounded = this.configuration.getObjective() == CommonGraphSolverObjective.BOUNDED_REACHABILITY;
    	assert unbounded || bounded;
    	if (unbounded) {
    		out.print(0 + " ");
    	} else if (bounded) {
    		out.print(1 + " " + this.configuration.getTime() + " ");
    	}
	}

	private void printInitialStates(int[] nodeToStateNumber, PrintStream out) {
        BitSet init = graph.getInitialNodes();
        out.print(init.cardinality());
        out.print(" ");
        for (int nodeNr = init.nextSetBit(0); nodeNr >= 0; nodeNr = init.nextSetBit(nodeNr+1)) {
            int state = nodeToStateNumber[nodeNr];
            out.print(state);
            out.print(" ");
        }
    }

    private void printTargetStates(int[] nodeToStateNumber, PrintStream out) {
    	assert out != null;
        BitSet target = this.configuration.getTarget();
        BitSet targetStates = new BitSet();
        for (int nodeNr = target.nextSetBit(0); nodeNr >= 0; nodeNr = target.nextSetBit(nodeNr+1)) {
            int state = nodeToStateNumber[nodeNr];
            if (state >= 0) {
            	targetStates.set(state);
            }
        }
        out.print(targetStates.cardinality());
        out.print(" ");
        for (int state = targetStates.nextSetBit(0); state >= 0; state = targetStates.nextSetBit(state+1)) {
            out.print(state);
            out.print(" ");
        }
    }

    private void printMatrix(int[] nodeToStateNumber, int nondetNr,
            PrintStream out) throws EPMCException {
        BitSet nodes = graph.getQueriedNodes();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        EdgeProperty weights = graph.getEdgeProperty(CommonProperties.WEIGHT);
        int nextBound = 0;
        out.print(nextBound + " ");
        for (int nodeNr = nodes.nextSetBit(0); nodeNr >= 0; nodeNr = nodes.nextSetBit(nodeNr+1)) {
            graph.queryNode(nodeNr);
            if (!stateProp.getBoolean()) {
                continue;
            }
            int succNr = nondetNr;
            if (succNr >= graph.getNumSuccessors()) {
                succNr = graph.getNumSuccessors() - 1;
            }
            int distribution = graph.getSuccessorNode(succNr);
            graph.queryNode(distribution);
            nextBound += graph.getNumSuccessors();
            out.print(nextBound + " ");
        }
        out.println();
        for (int nodeNr = nodes.nextSetBit(0); nodeNr >= 0; nodeNr = nodes.nextSetBit(nodeNr+1)) {
            graph.queryNode(nodeNr);
            if (!stateProp.getBoolean()) {
                continue;
            }
            int succNr = nondetNr;
            if (succNr >= graph.getNumSuccessors()) {
                succNr = graph.getNumSuccessors() - 1;
            }
            int distribution = graph.getSuccessorNode(succNr);
            graph.queryNode(distribution);
            int numSuccessors = graph.getNumSuccessors();
            for (int dNr = 0; dNr < numSuccessors; dNr++) {
                int succNode = graph.getSuccessorNode(dNr);
                int succState = nodeToStateNumber[succNode];
                out.print(succState + " ");
            }
        }
        out.println();
        for (int nodeNr = nodes.nextSetBit(0); nodeNr >= 0; nodeNr = nodes.nextSetBit(nodeNr+1)) {
            graph.queryNode(nodeNr);
            if (!stateProp.getBoolean()) {
                continue;
            }
            int succNr = nondetNr;
            if (succNr >= graph.getNumSuccessors()) {
                succNr = graph.getNumSuccessors() - 1;
            }
            int distribution = graph.getSuccessorNode(succNr);
            graph.queryNode(distribution);
            int numSuccessors = graph.getNumSuccessors();
            for (int dNr = 0; dNr < numSuccessors; dNr++) {
                out.print(weights.get(dNr) + " ");
            }
        }        
    }

    private int[] computeNodeToStateNumber() throws EPMCException {
        int[] result = new int[graph.getQueriedNodesLength()];
        Arrays.fill(result, -1);
        BitSet nodes = graph.getQueriedNodes();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        int state = 0;
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            graph.queryNode(node);
            if (stateProp.getBoolean()) {
                result[node] = state;
                state++;
            }
        }
        return result;
    }

    private int countMaxActionFanout() throws EPMCException {
        int maxActionFanout = 0;
        BitSet nodes = graph.getQueriedNodes();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        for (int state = nodes.nextSetBit(0); state >= 0; state = nodes.nextSetBit(state+1)) {
            graph.queryNode(state);
            if (!stateProp.getBoolean()) {
                maxActionFanout = Math.max(maxActionFanout, graph.getNumSuccessors());
            }
        }
        return maxActionFanout;
    }

    private int countNumStates() throws EPMCException {
        int numStates = 0;
        BitSet nodes = graph.getQueriedNodes();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        for (int state = nodes.nextSetBit(0); state >= 0; state = nodes.nextSetBit(state+1)) {
            graph.queryNode(state);
            if (stateProp.getBoolean()) {
                numStates++;
            }
        }
        return numStates;
    }

    private int countMaxNondetFanout() throws EPMCException {
        int maxNondetFanout = 0;
        BitSet nodes = graph.getQueriedNodes();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        for (int state = nodes.nextSetBit(0); state >= 0; state = nodes.nextSetBit(state+1)) {
            graph.queryNode(state);
            if (stateProp.getBoolean()) {
                maxNondetFanout = Math.max(maxNondetFanout, graph.getNumSuccessors());
            }
        }
        return maxNondetFanout;
    }

    @Override
    public Value getResult() {
        return graph.getContextValue().getTypeWeight().getTypeArray().newValue(graph.getQueriedNodesLength());
    }

    @Override
    public Value getScheduler() {
        // TODO Auto-generated method stub
        return null;
    }

}
