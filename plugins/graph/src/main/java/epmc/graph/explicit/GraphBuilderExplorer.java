package epmc.graph.explicit;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.MessagesGraph;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicitSparseAlternate.EdgePropertySparseNondet;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNode;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.util.BitStoreableToNumber;
import epmc.util.StopWatch;
import epmc.value.Type;
import epmc.value.ValueObject;

/**
 * Build a graph representation from an explorer.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphBuilderExplorer {
    private Explorer explorer;
    private final Set<Object> graphProperties = new LinkedHashSet<>();
    private final Set<Object> nodeProperties = new LinkedHashSet<>();
    private final Set<Object> edgeProperties = new LinkedHashSet<>();
    private GraphExplicit graph;
    private GraphExplicitSparse graphStoch;
    private GraphExplicitSparseAlternate graphAlter;
    private Log log;
    private int lastState;
    private int currentState;
    private boolean done;

    public void setExplorer(Explorer explorer) {
        assert explorer != null;
        this.explorer = explorer;
        this.log = explorer.getOptions().get(OptionsMessages.LOG);
    }

    public void addDerivedGraphProperties(Set<Object> graphProperties) {
        assert graphProperties != null;
        this.graphProperties.addAll(graphProperties);
    }

    public void addDerivedNodeProperties(Set<Object> nodeProperties) {
        assert nodeProperties != null;
        this.nodeProperties.addAll(nodeProperties);
    }

    public void addDerivedEdgeProperties(Set<Object> edgeProperties) {
        assert edgeProperties != null;
        this.edgeProperties.addAll(edgeProperties);
    }

    public void build() throws EPMCException {
        Thread observerThread = new Thread(() -> {
            int sleepTime = 5;
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (Exception e) {
            }
            while (!done) {
                int delta = currentState - lastState;
                delta /= sleepTime;
                lastState = currentState;
                try {
                    log.send(MessagesGraph.BUILD_MODEL_STATES_EXPLORED, currentState, delta);
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (Exception e) {
                }
            }
        });
        observerThread.start();

        try {
            doBuild();
        } catch (EPMCException e) {
            done = true;
            observerThread.interrupt();
            throw e;
        } catch (Throwable e) {
            done = true;
            observerThread.interrupt();
            throw new RuntimeException(e);
        }
        done = true;
        observerThread.interrupt();
    }

    private void doBuild() throws EPMCException {
        assert this.explorer != null;
        assert this.graphProperties != null;
        assert this.nodeProperties != null;
        assert this.edgeProperties != null;
        StopWatch watch = new StopWatch(true);
        log.send(MessagesGraph.BUILD_MODEL_START);
        BitStoreableToNumber nodeStore = newNodeStore(explorer);
        int lastNumber = 0;
        int numInitStates = 0;
        for (ExplorerNode node : explorer.getInitialNodes()) {            
            int number = nodeStore.toNumber(node);
            lastNumber = number;
            numInitStates++;
        }
        // TODO ensure that there is at least one state
        ExplorerNode currentNode = explorer.newNode();
        Semantics semantics = ValueObject.asObject(explorer.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean nondet = semantics != null && SemanticsNonDet.isNonDet(semantics);
        ExplorerNode[] successorNodes = new ExplorerNode[1];
        successorNodes[0] = explorer.newNode();
        this.currentState = 0;
        
        int numStates = lastNumber + 1;
        if (nondet) {
            graphAlter = new GraphExplicitSparseAlternate(explorer.getContextValue(), false);
            this.graph = graphAlter;
        } else {
            graphStoch = new GraphExplicitSparse(explorer.getContextValue(), false);
            this.graph = graphStoch;
        }
        for (Object property : graphProperties) {
            Type type = explorer.getGraphPropertyType(property);
            this.graph.addSettableGraphProperty(property, type);
            this.graph.setGraphProperty(property, explorer.getGraphProperty(property));
        }
        boolean withExplorerNode = nodeProperties.contains(CommonProperties.NODE_EXPLORER);
        nodeProperties.remove(CommonProperties.NODE_EXPLORER);
        ExplorerNodeProperty[] explorerNodeProperties = new ExplorerNodeProperty[nodeProperties.size()];
        NodeProperty[] graphNodeProperties = new NodeProperty[nodeProperties.size()];
        int nodePropNr = 0;
        for (Object property : nodeProperties) {
            Type type = explorer.getNodePropertyType(property);
            assert type != null : property + " " + explorer.getNodeProperty(property);
            explorerNodeProperties[nodePropNr] = explorer.getNodeProperty(property);
            graphNodeProperties[nodePropNr] = this.graph.addSettableNodeProperty(property, type);
            nodePropNr++;
        }
        if (withExplorerNode) {
            graph.registerNodeProperty(CommonProperties.NODE_EXPLORER,
                    new NodePropertyExplorerNode(graph, explorer, nodeStore));
        }
        ExplorerEdgeProperty[] explorerEdgeProperties = new ExplorerEdgeProperty[edgeProperties.size()];
        EdgePropertySparseNondet[] graphEdgePropertiesAlter = new EdgePropertySparseNondet[edgeProperties.size()];
        EdgeProperty[] graphEdgeProperties = new EdgeProperty[edgeProperties.size()];
        int edgePropNr = 0;
        if (nondet) {
            for (Object property : edgeProperties) {
                Type type = explorer.getEdgePropertyType(property);
                explorerEdgeProperties[edgePropNr] = explorer.getEdgeProperty(property);
                graphEdgePropertiesAlter[edgePropNr] = graphAlter.addSettableEdgeProperty(property, type);
                graphEdgeProperties[edgePropNr] = graphEdgePropertiesAlter[edgePropNr];
                edgePropNr++;
            }
        } else {
            for (Object property : edgeProperties) {
                Type type = explorer.getEdgePropertyType(property);
                explorerEdgeProperties[edgePropNr] = explorer.getEdgeProperty(property);
                graphEdgeProperties[edgePropNr] = graph.addSettableEdgeProperty(property, type);
                edgePropNr++;
            }
        }
        int nondetNr = numStates;
        lastState = 0;
        this.currentState = 0;
        while (currentState <= lastNumber) {
            graph.queryNode(currentState);
            nodeStore.fromNumber(currentNode, currentState);
            explorer.queryNode(currentNode);
            int numStateSuccessors = explorer.getNumSuccessors();
            if (nondet) {
                graphAlter.prepareState(numStateSuccessors);
            } else {
                graphStoch.prepareNode(numStateSuccessors);
            }
            for (nodePropNr = 0; nodePropNr < graphNodeProperties.length; nodePropNr++) {
                graphNodeProperties[nodePropNr].set(explorerNodeProperties[nodePropNr].get());
            }
            if (nondet) {
                successorNodes = assignSuccessorNodes(successorNodes);
                for (int stateSuccNr = 0; stateSuccNr < numStateSuccessors; stateSuccNr++) {
                    for (nodePropNr = 0; nodePropNr < graphEdgeProperties.length; nodePropNr++) {
                        graphEdgePropertiesAlter[nodePropNr].setForState(explorerEdgeProperties[nodePropNr].get(stateSuccNr), stateSuccNr);
                    }
                    nondetNr++;
                }
                nondetNr -= numStateSuccessors;
                for (int stateSuccNr = 0; stateSuccNr < numStateSuccessors; stateSuccNr++) {
                    graphAlter.queryNode(nondetNr);
                    explorer.queryNode(successorNodes[stateSuccNr]);
                    for (nodePropNr = 0; nodePropNr < graphNodeProperties.length; nodePropNr++) {
                        graphNodeProperties[nodePropNr].set(explorerNodeProperties[nodePropNr].get());
                    }
                    int numISuccessors = explorer.getNumSuccessors();
                    graphAlter.prepareNondet(numISuccessors);            
                    for (int interSuccNr = 0; interSuccNr < numISuccessors; interSuccNr++ ){
                        for (nodePropNr = 0; nodePropNr < graphEdgeProperties.length; nodePropNr++) {
                            graphEdgePropertiesAlter[nodePropNr].setForNonDet(explorerEdgeProperties[nodePropNr].get(interSuccNr), interSuccNr);
                        }
                        int numberSucc = nodeStore.toNumber(explorer.getSuccessorNode(interSuccNr));
                        graphAlter.setNondetSuccessor(interSuccNr, numberSucc);
//                        graph.setSuccessorNode(interSuccNr, numberSucc);
                        lastNumber = Math.max(numberSucc, lastNumber);
                    }
                    nondetNr++;
                }
            } else {
                for (int stateSuccNr = 0; stateSuccNr < numStateSuccessors; stateSuccNr++) {
                    int numberSucc = nodeStore.toNumber(explorer.getSuccessorNode(stateSuccNr));
                    graph.setSuccessorNode(stateSuccNr, numberSucc);
                    for (nodePropNr = 0; nodePropNr < graphEdgeProperties.length; nodePropNr++) {
                        graphEdgeProperties[nodePropNr].set(explorerEdgeProperties[nodePropNr].get(stateSuccNr), stateSuccNr);
                    }
                    lastNumber = Math.max(numberSucc, lastNumber);
                }
            }
            currentState++;
        }
        for (int initState = 0; initState < numInitStates; initState++) {
            this.graph.getInitialNodes().set(initState);
        }
        log.send(MessagesGraph.BUILD_MODEL_DONE, currentState, watch.getTimeSeconds());
        if (graph instanceof GraphExplicitSparse) {
//            graphStoch.setNumStates(numStates);
//            ((GraphExplicitSparse) graph).setNumStates(numStates);
        } else if (graph instanceof GraphExplicitSparseAlternate) {
//            ((GraphExplicitSparseAlternate) graph).setNumStates(numStates);
        }
    }
    
    private ExplorerNode[] assignSuccessorNodes(ExplorerNode[] successorNodes) throws EPMCException {
        int numSuccessors = explorer.getNumSuccessors();
        if (numSuccessors > successorNodes.length) {
            int oldLength = successorNodes.length;
            int newLength = oldLength;
            while (newLength < numSuccessors) {
                newLength *= 2;
            }
            successorNodes = Arrays.copyOf(successorNodes, newLength);
            for (int succ = oldLength; succ < newLength; succ++) {
                successorNodes[succ] = explorer.newNode();
            }
        }
        for (int succ = 0; succ < numSuccessors; succ++) {
            successorNodes[succ].set(explorer.getSuccessorNode(succ));
        }
        return successorNodes;
    }

    private static BitStoreableToNumber newNodeStore(Explorer explorer)
            throws EPMCException {
        int numBits = explorer.getNumNodeBits();
        return UtilGraph.newNodeStore(explorer.getOptions(), numBits);
    }

    public GraphExplicit getGraph() {
        assert this.graph != null;
        return this.graph;
    }

}