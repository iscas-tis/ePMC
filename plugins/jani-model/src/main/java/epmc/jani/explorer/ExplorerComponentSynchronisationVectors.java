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

package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import epmc.graph.CommonProperties;
import epmc.graph.SemanticsNonDet;
import epmc.graph.SemanticsStochastic;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.Edge;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.SynchronisationVectorElement;
import epmc.jani.model.component.SynchronisationVectorSync;
import epmc.operator.OperatorMultiply;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

// TODO compute labels in all cases

/**
 * Explorer for synchronisation vectors.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExplorerComponentSynchronisationVectors implements ExplorerComponent {
    /** Explorer for which this explorer component is used. */
    private ExplorerJANI explorer;
    /** Component which this explorer is supposed to explore. */
    private ComponentSynchronisationVectors componentSynchronisationVectors;
    /** Automata used in this synchronisation vector. */
    private ExplorerComponentAutomaton[] automata;
    /** Label edge property of component the actions of which are renamed. */
    private PropertyEdgeAction[] automataLabels;
    private PropertyEdge[] automataWeights;
    /** Label edge property. */
    private PropertyEdgeAction label;
    /** Model component from which this explorer component shall be generated. */
    private Component component;
    /** Node property indicating whether node is a state. */
    private PropertyNodeGeneral state;
    /** Whether this model can contain non-state nodes. */
    private boolean twoLayer;
    /** Number of synchronisation vectors used. */
    private int numVectors;
    private int[] vectorSizes;
    private int[][] vectorActions;
    private int[][] vectorAutomata;
    private int[] vectorResult;
    /** Number of successors of queried node. */
    private int numSuccessors;
    /** Array of successors of queried node. */
    private NodeJANI[] successors;
    private PropertyEdgeGeneral weight;
    private ValueAlgebra prodWeight;
    private boolean[] isState;
    private OperatorEvaluator multiply;

    @Override
    public void setExplorer(ExplorerJANI model) {
        assert this.explorer == null;
        assert model != null;
        this.explorer = model;
    }

    @Override
    public void setComponent(Component component) {
        assert component != null;
        this.component = component;
    }

    @Override
    public boolean canHandle() {
        if (!(component instanceof ComponentSynchronisationVectors)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        assert explorer != null;
        assert component != null;
        componentSynchronisationVectors = (ComponentSynchronisationVectors) component;
        List<SynchronisationVectorElement> elements = componentSynchronisationVectors.getElements();
        automata = new ExplorerComponentAutomaton[elements.size()];
        automataLabels = new PropertyEdgeAction[elements.size()];
        automataWeights = new PropertyEdge[elements.size()];
        int autNr = 0;
        for (SynchronisationVectorElement element : elements) {
            ComponentAutomaton componentAutomaton = new ComponentAutomaton();
            componentAutomaton.setModel(explorer.getModel());
            componentAutomaton.setAutomaton(element.getAutomaton());
            automata[autNr] = new ExplorerComponentAutomaton();
            automata[autNr].setExplorer(explorer);
            automata[autNr].setComponent(componentAutomaton);
            automata[autNr].build();
            automataLabels[autNr] = (PropertyEdgeAction) automata[autNr].getEdgeProperty(CommonProperties.TRANSITION_LABEL);
            automataWeights[autNr] = automata[autNr].getEdgeProperty(CommonProperties.WEIGHT);
            autNr++;
        }
        state = new PropertyNodeGeneral(explorer, TypeBoolean.get());
        label = new PropertyEdgeAction(explorer);
        twoLayer = SemanticsNonDet.isNonDet(explorer.getModel().getSemantics())
                && SemanticsStochastic.isStochastic(explorer.getModel().getSemantics());
        List<SynchronisationVectorSync> syncVectors = new ArrayList<>();
        syncVectors.addAll(componentSynchronisationVectors.getSyncs());
        Map<Action, Integer> actionMap = UtilExplorer.computeActionToInteger(explorer.getModel());
        // TODO ??
//        syncVectors.addAll(computeSilentVectors(actionMap));
        numVectors = syncVectors.size();
        vectorSizes = new int[syncVectors.size()];
        vectorResult = new int[syncVectors.size()];
        vectorActions = new int[syncVectors.size()][];
        vectorAutomata = new int[syncVectors.size()][];
        int vecNr = 0;
        for (SynchronisationVectorSync syncVec : syncVectors) {
            List<Action> sync = syncVec.getSynchronise();
            int syncSize = 0;
            for (Action action : sync) {
                autNr++;
                if (action == null) {
                    continue;
                }
                syncSize++;
            }
            vectorAutomata[vecNr] = new int[syncSize];
            vectorActions[vecNr] = new int[syncSize];
            vectorSizes[vecNr] = syncSize;
            int elemNr = 0;
            autNr = -1;
            for (Action action : sync) {
                autNr++;
                if (action == null) {
                    continue;
                }
                int actionNr = actionMap.get(action);
                vectorActions[vecNr][elemNr] = actionNr;
                vectorAutomata[vecNr][elemNr] = autNr;
                elemNr++;
            }
            int resultAction = actionMap.get(syncVec.getResult());
            vectorResult[vecNr] = resultAction;
            vecNr++;
        }
        prodWeight = TypeWeightTransition.get().newValue();
        weight = new PropertyEdgeGeneral(explorer, TypeWeightTransition.get());
        isState = new boolean[automata.length];
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeightTransition.get(), TypeWeightTransition.get());
    }

    private List<SynchronisationVectorSync> computeSilentVectors(Map<Action, Integer> actionMap) {
        Action silentAction = componentSynchronisationVectors.getModel().getSilentAction();
        int silentActionNr = actionMap.get(silentAction);
        List<SynchronisationVectorSync> result = new ArrayList<>();
        List<SynchronisationVectorElement> elements = componentSynchronisationVectors.getElements();
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            SynchronisationVectorElement element = elements.get(elementNr);
            Automaton automaton = element.getAutomaton();
            boolean automatonHasSilentAction = false;
            for (Edge edge : automaton.getEdges()) {
                int edgeActionNr = actionMap.get(edge.getActionOrSilent());
                if (silentActionNr == edgeActionNr) {
                    automatonHasSilentAction = true;
                    break;
                }
            }
            if (!automatonHasSilentAction) {
                continue;
            }
            SynchronisationVectorSync sync = new SynchronisationVectorSync();
            sync.setResult(silentAction);
            List<Action> syncList = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                syncList.add(null);
            }
            syncList.set(elementNr, silentAction);
            sync.setSynchronise(syncList);
            result.add(sync);
        }
//        List<SynchronisationVectorSync> result = new ArrayList<>();
//        explorer.getModel().getAutomata()
        // TODO Auto-generated method stub
        return result;
    }

    @Override
    public void buildAfterVariables() {
        successors = new NodeJANI[1];
        successors[0] = newNode();
        for (ExplorerComponentAutomaton automaton : automata) {
            automaton.buildAfterVariables();
        }
    }

    @Override
    public void queryNode(NodeJANI node) {
        assert node != null;
        if (twoLayer) {
            queryNodeTwoLayer(node);
        } else {
            queryNodeSingleLayer(node);
        }
    }

    private void queryNodeTwoLayer(NodeJANI node) {
        int autNr = 0;
        for (ExplorerComponentAutomaton automaton : automata) {
            isState[autNr] = automaton.isState(node);
            autNr++;
        }
        boolean state = isState(node);
        this.state.set(state);
        if (state) {
            queryNodeTwoLayerState(node);
        } else {
            queryNodeTwoLayerNonState(node);
        }
    }

    private void queryNodeTwoLayerState(NodeJANI node) {
        for (int automatonNr = 0; automatonNr < automata.length; automatonNr++) {
            automata[automatonNr].queryNode(node);
        }
        numSuccessors = 0;
        for (int vecNr = 0; vecNr < numVectors; vecNr++) {
            int numVecSuccessors = 1;
            int vecSize = vectorSizes[vecNr];
            for (int entryNr = 0; entryNr < vecSize; entryNr++) {
                int autNr = vectorAutomata[vecNr][entryNr];
                ExplorerComponentAutomaton automaton = automata[autNr];
                int action = vectorActions[vecNr][entryNr];
                int numAutSucc = automaton.getActionTo(action)
                        - automaton.getActionFrom(action);
                numVecSuccessors *= numAutSucc;
            }
            for (int succNr = 0; succNr < numVecSuccessors; succNr++) {
                ensureSuccessorsSize();
                int remaining = succNr;
                NodeJANI successor = successors[numSuccessors];
                successor.set(node);
                successor.unmark();
                for (int entryNr = 0; entryNr < vecSize; entryNr++) {
                    int autNr = vectorAutomata[vecNr][entryNr];
                    ExplorerComponentAutomaton automaton = automata[autNr];
                    int action = vectorActions[vecNr][entryNr];
                    int actionFrom = automaton.getActionFrom(action);
                    int numAutSucc = automaton.getActionTo(action)
                            - actionFrom;
                    int autSucc = (remaining % numAutSucc) + actionFrom;
                    remaining /= numAutSucc;
                    successor.setSet(automaton.getSuccessorNode(autSucc));
                }
                label.set(numSuccessors, vectorResult[vecNr]);
                numSuccessors++;
            }
        }
    }

    private void queryNodeTwoLayerNonState(NodeJANI node) {
        int numSuccessors = 1;
        for (int autNr = 0; autNr < automata.length; autNr++) {
            ExplorerComponentAutomaton automaton = automata[autNr];
            if (!isState[autNr]) {
                automaton.queryNode(node);
                numSuccessors *= automaton.getNumSuccessors();
            }
        }
        this.numSuccessors = 0;
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            ensureSuccessorsSize();
            int remaining = succNr;
            prodWeight.set(1);
            NodeJANI successor = successors[this.numSuccessors];
            successor.set(node);
            successor.unmark();
            for (int autNr = 0; autNr < automata.length; autNr++) {
                ExplorerComponentAutomaton automaton = automata[autNr];
                if (isState[autNr]) {
                    continue;
                }
                int numAutSuccessors = automaton.getNumSuccessors();
                int autSuccNr = remaining % numAutSuccessors;
                remaining /= numAutSuccessors;
                successor.setSet(automaton.getSuccessorNode(autSuccNr));
                PropertyEdge automatonWeight = this.automataWeights[autNr];
                multiply.apply(prodWeight, prodWeight, automatonWeight.get(autSuccNr));
            }
            weight.set(this.numSuccessors, prodWeight);
            this.numSuccessors++;
        }
    }

    private void queryNodeSingleLayer(NodeJANI node) {
        assert node != null;
        for (int automatonNr = 0; automatonNr < automata.length; automatonNr++) {
            automata[automatonNr].queryNode(node);
        }
        numSuccessors = 0;
        for (int vecNr = 0; vecNr < numVectors; vecNr++) {
            int numVecSuccessors = 1;
            int vecSize = vectorSizes[vecNr];
            for (int entryNr = 0; entryNr < vecSize; entryNr++) {
                int autNr = vectorAutomata[vecNr][entryNr];
                ExplorerComponentAutomaton automaton = automata[autNr];
                int action = vectorActions[vecNr][entryNr];
                int numAutSucc = automaton.getActionTo(action)
                        - automaton.getActionFrom(action);
                numVecSuccessors *= numAutSucc;
            }
            for (int succNr = 0; succNr < numVecSuccessors; succNr++) {
                prodWeight.set(1);
                ensureSuccessorsSize();
                int remaining = succNr;
                NodeJANI successor = successors[numSuccessors];
                successor.set(node);
                successor.unmark();
                for (int entryNr = 0; entryNr < vecSize; entryNr++) {
                    int autNr = vectorAutomata[vecNr][entryNr];
                    ExplorerComponentAutomaton automaton = automata[autNr];
                    int action = vectorActions[vecNr][entryNr];
                    int actionFrom = automaton.getActionFrom(action);
                    int numAutSucc = automaton.getActionTo(action)
                            - actionFrom;
                    int autSucc = (remaining % numAutSucc) + actionFrom;
                    remaining /= numAutSucc;
                    successor.setSet(automaton.getSuccessorNode(autSucc));
                    PropertyEdge automatonWeight = this.automataWeights[autNr];
                    multiply.apply(prodWeight, prodWeight, automatonWeight.get(autSucc));
                }
                weight.set(numSuccessors, prodWeight);
                numSuccessors++;
            }
        }
    }

    @Override
    public int getNumSuccessors() {
        return numSuccessors;
    }

    @Override
    public NodeJANI getSuccessorNode(int succNr) {
        assert succNr >= 0;
        assert succNr < numSuccessors;
        return successors[succNr];
    }

    @Override
    public Value getGraphProperty(Object property) {
        assert property != null;
        for (ExplorerComponentAutomaton automaton : automata) {
            Value graphProperty = automaton.getGraphProperty(property);
            if (graphProperty != null) {
                return graphProperty;
            }
        }
        return null;
    }

    @Override
    public PropertyNode getNodeProperty(Object property) {
        assert property != null;
        if (property == CommonProperties.STATE) {
            return state;
        } else {
            return null;
        }
    }

    @Override
    public PropertyEdge getEdgeProperty(Object property) {
        assert property != null;
        assert property != null;
        if (property == CommonProperties.WEIGHT) {
            return weight;
        } else if (property == CommonProperties.TRANSITION_LABEL) {
            return label;
        } else {
            return null;
        }
    }

    @Override
    public Collection<NodeJANI> getInitialNodes() {		
        List<NodeJANI> initialNodes = Collections.singletonList(explorer.newNode());
        for (ExplorerComponentAutomaton automaton : automata) {
            List<NodeJANI> newInitialNodes = new ArrayList<>();
            for (NodeJANI existingInitNode : initialNodes) {
                for (NodeJANI automatonInitNode : automaton.getInitialNodes()) {
                    NodeJANI newInitialNode = explorer.newNode();
                    newInitialNode.setSet(existingInitNode);
                    newInitialNode.setSet(automatonInitNode);
                    newInitialNodes.add(newInitialNode);
                }
            }
            initialNodes = newInitialNodes;
        }
        return initialNodes;
    }

    @Override
    public ExplorerJANI getExplorer() {
        return explorer;
    }

    @Override
    public int getNumNodeBits() {
        return explorer.getNumNodeBits();
    }

    @Override
    public NodeJANI newNode() {
        return explorer.newNode();
    }

    @Override
    public void setNumSuccessors(int numSuccessors) {
        this.numSuccessors = numSuccessors;
    }

    @Override
    public boolean isState(NodeJANI node) {
        boolean state = true;
        for (boolean is : isState) {
            state &= is;
        }
        return state;
    }

    @Override
    public boolean isState() {
        return state.getBoolean();
    }

    @Override
    public void close() {
        for (ExplorerComponentAutomaton automaton : automata) {
            automaton.close();
        }
    }

    /**
     * Ensure successors size array sufficiently large to store successors.
     */
    private void ensureSuccessorsSize() {
        if (numSuccessors < successors.length) {
            return;
        }
        int newLength = successors.length;
        while (newLength <= numSuccessors) {
            newLength *= 2;
        }
        NodeJANI[] newSuccessors = Arrays.copyOf(successors, newLength);
        for (int newSuccNr = successors.length; newSuccNr < newSuccessors.length; newSuccNr++) {
            newSuccessors[newSuccNr] = newNode();
        }
        successors = newSuccessors;
    }
}
