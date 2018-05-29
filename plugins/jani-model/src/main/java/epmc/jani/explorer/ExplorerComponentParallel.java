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

import static epmc.error.UtilError.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import epmc.graph.CommonProperties;
import epmc.graph.SemanticsNonDet;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentParallel;
import epmc.operator.OperatorMultiply;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

/**
 * Explorer for a parallel composition component.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExplorerComponentParallel implements ExplorerComponent {
    private ExplorerJANI explorer;
    /** Component which this explorer is supposed to explore. */
    private ComponentParallel componentParallel;
    /** Explorer for left part of parallel composition. */
    private ExplorerComponent left;
    /** Explorer for right part of parallel composition. */
    private ExplorerComponent right;
    /** Number of successors of queried node. */
    private int numSuccessors;
    /** Array of successors of queried node. */
    private NodeJANI[] successors;
    /** Value to compute product of weight of left and right weights. */
    private ValueAlgebra weightProduct;
    /** Property to store whether given node is a state. */
    private PropertyNodeGeneral state;
    /** Transition weight property. */
    private PropertyEdgeGeneral weight;
    /** Label/action property. */
    private PropertyEdgeAction label;
    /** Weight property of left component explorer. */
    private PropertyEdge leftWeight;
    /** Weight property of right component explorer. */
    private PropertyEdge rightWeight;
    /** Label/action property of left component explorer. */
    private PropertyEdgeAction leftLabel;
    /** Label/action property of right component explorer. */
    private PropertyEdgeAction rightLabel;
    /** Synchronising actions of the composition explorer. */
    private boolean[] synchronisingActions;
    /** Silent action of the model. */
    private boolean nonDet;
    private Component component;
    private OperatorEvaluator multiply;

    // TODO check for LTSs as soon as we have some

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        assert this.explorer == null;
        assert explorer != null;
        this.explorer = explorer;
    }

    @Override
    public void setComponent(Component component) {
        assert component != null;
        this.component = component;
    }

    @Override
    public boolean canHandle() {
        if (!(component instanceof ComponentParallel)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        assert explorer != null;
        assert component != null;
        this.componentParallel = (ComponentParallel) component;
        this.nonDet = SemanticsNonDet.isNonDet(explorer.getModel().getSemantics());
        PreparatorComponentExplorer preparator = new PreparatorComponentExplorer();
        left = preparator.prepare(explorer, componentParallel.getLeft());
        right = preparator.prepare(explorer, componentParallel.getRight());
        state = new PropertyNodeGeneral(this, TypeBoolean.get());
        weight = new PropertyEdgeGeneral(this, TypeWeightTransition.get());
        label = new PropertyEdgeAction(explorer);
        leftWeight = left.getEdgeProperty(CommonProperties.WEIGHT);
        assert leftWeight != null;
        rightWeight = right.getEdgeProperty(CommonProperties.WEIGHT);
        assert rightWeight != null;
        weightProduct = TypeWeightTransition.get().newValue();
        leftLabel = (PropertyEdgeAction) left.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        assert leftLabel != null;
        rightLabel = (PropertyEdgeAction) right.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        assert rightLabel != null;
        synchronisingActions = buildSynchronisingActions(new HashSet<>(componentParallel.getActions()));
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeightTransition.get(), TypeWeightTransition.get());
    }

    private boolean[] buildSynchronisingActions(Set<Action> synchronising) {
        ModelJANI model = explorer.getModel();
        Actions actions = model.getActionsOrEmpty();
        boolean[] result = new boolean[actions.size() + 1];
        int actionNumber = 0;
        result[actionNumber] = synchronising.contains(model.getSilentAction());
        actionNumber++;
        for (Action action : actions) {
            result[actionNumber] = synchronising.contains(action);
            actionNumber++;
        }
        return result;
    }

    @Override
    public void buildAfterVariables() {
        successors = new NodeJANI[1];
        successors[0] = newNode();
        left.buildAfterVariables();
        right.buildAfterVariables();
    }

    @Override
    public Value getGraphProperty(Object property) {
        assert property != null;
        if (left.getGraphProperty(property) != null) {
            return left.getGraphProperty(property);
        } else if (right.getGraphProperty(property) != null) {
            return right.getGraphProperty(property);
        } else {
            return null;
        }
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
        Collection<NodeJANI> leftNodes = left.getInitialNodes();
        Collection<NodeJANI> rightNodes = right.getInitialNodes();
        Collection<NodeJANI> result = new ArrayList<>();
        for (NodeJANI leftNode : leftNodes) {
            for (NodeJANI rightNode : rightNodes) {
                NodeJANI combined = newNode();
                combined.setSet(leftNode);
                combined.setSet(rightNode);
                result.add(combined);
            }
        }
        return result;
    }

    @Override
    public void queryNode(NodeJANI node) {
        assert node != null;
        if (nonDet) {
            queryNonDet(node);
        } else {
            queryNoNonDet(node);
        }
    }

    private void queryNoNonDet(NodeJANI node) {
        numSuccessors = 0;
        left.queryNode(node);
        right.queryNode(node);
        int numLeftSuccessors = left.getNumSuccessors();
        int numRightSuccessors = right.getNumSuccessors();
        for (int leftSuccNr = 0; leftSuccNr < numLeftSuccessors; leftSuccNr++) {
            int leftAction = leftLabel.getInt(leftSuccNr);
            if (!synchronisingActions[leftAction]) {
                continue;
            }
            NodeJANI leftSuccessor = left.getSuccessorNode(leftSuccNr);
            for (int rightSuccNr = 0; rightSuccNr < numRightSuccessors; rightSuccNr++) {
                if (leftAction != rightLabel.getInt(rightSuccNr)) {
                    continue;
                }
                ensureSuccessorsSize();
                NodeJANI successor = successors[numSuccessors];
                successor.unmark();
                successor.setAndMark(leftSuccessor);
                if (!successor.setSet(right.getSuccessorNode(rightSuccNr))) {
                    fail(ProblemsJANIExplorer.JANI_EXPLORER_GLOBAL_MULTIPLE);
                }
                label.set(numSuccessors, leftAction);
                multiply.apply(weightProduct, leftWeight.get(leftSuccNr), rightWeight.get(rightSuccNr));
                weight.set(numSuccessors, weightProduct);
                numSuccessors++;
            }
        }
        for (int leftSuccNr = 0; leftSuccNr < numLeftSuccessors; leftSuccNr++) {
            int leftAction = leftLabel.getInt(leftSuccNr);
            if (synchronisingActions[leftAction]) {
                continue;
            }
            ensureSuccessorsSize();
            NodeJANI successor = successors[numSuccessors];
            successor.unmark();
            successor.setAndMark(left.getSuccessorNode(leftSuccNr));
            label.set(numSuccessors, leftAction);
            weight.set(numSuccessors, leftWeight.get(leftSuccNr));
            numSuccessors++;
        }
        for (int rightSuccNr = 0; rightSuccNr < numRightSuccessors; rightSuccNr++) {
            int rightAction = rightLabel.getInt(rightSuccNr);
            if (synchronisingActions[rightAction]) {
                continue;
            }
            ensureSuccessorsSize();
            NodeJANI successor = successors[numSuccessors];
            successor.unmark();
            successor.setAndMark(right.getSuccessorNode(rightSuccNr));
            label.set(numSuccessors, rightAction);
            weight.set(numSuccessors, rightWeight.get(rightSuccNr));
            numSuccessors++;
        }
    }

    private void queryNonDet(NodeJANI node) {
        numSuccessors = 0;
        boolean isLeftState = left.isState(node);
        boolean isRightState = right.isState(node);
        if (isLeftState && isRightState) {
            left.queryNode(node);
            right.queryNode(node);
            querySynchroniseActions(node);
        } else if (isLeftState && !isRightState) {
            right.queryNode(node);
            queryCopy(node, true);
        } else if (!isLeftState && isRightState) {
            left.queryNode(node);
            queryCopy(node, false);			
        } else {
            left.queryNode(node);
            right.queryNode(node);
            queryMultiplyProbabilities(node);
        }
    }

    /**
     * Query implementation for action synchronisation case.
     * This method shall be used if the left and right node are both states,
     * that is, no action has been chosen for any of them yet. In this case, we
     * have to check the left and right successors and see which ones will be
     * synchronised and which ones just copied.
     * The node parameter may not be {@code null}. Note that is the query to
     * the left and right explorers must have been already performed.
     * 
     * @param node node to query
     */
    private void querySynchroniseActions(NodeJANI node) {
        assert node != null;
        state.set(true);
        int numLeftSuccessors = left.getNumSuccessors();
        int numRightSuccessors = right.getNumSuccessors();
        ValueAlgebra realZero = UtilValue.newValue(TypeReal.get(), 0);
        for (int leftSuccNr = 0; leftSuccNr < numLeftSuccessors; leftSuccNr++) {
            int leftAction = leftLabel.getInt(leftSuccNr);
            if (!synchronisingActions[leftAction]) {
                continue;
            }
            for (int rightSuccNr = 0; rightSuccNr < numRightSuccessors; rightSuccNr++) {
                int rightAction = rightLabel.getInt(rightSuccNr);
                if (leftAction != rightAction) {
                    continue;
                }
                ensureSuccessorsSize();
                NodeJANI successor = successors[numSuccessors];
                successor.unmark();
                successor.setAndMark(left.getSuccessorNode(leftSuccNr));
                successor.setSet(right.getSuccessorNode(rightSuccNr));
                label.set(numSuccessors, leftAction);
                weight.set(numSuccessors, realZero);
                numSuccessors++;
            }
        }
        for (int leftSuccNr = 0; leftSuccNr < numLeftSuccessors; leftSuccNr++) {
            int leftAction = leftLabel.getInt(leftSuccNr);
            if (synchronisingActions[leftAction]) {
                continue;
            }
            ensureSuccessorsSize();
            NodeJANI successor = successors[numSuccessors];
            successor.unmark();
            successor.setAndMark(left.getSuccessorNode(leftSuccNr));
            label.set(numSuccessors, leftAction);
            weight.set(numSuccessors, realZero);
            numSuccessors++;
        }
        for (int rightSuccNr = 0; rightSuccNr < numRightSuccessors; rightSuccNr++) {
            int rightAction = rightLabel.getInt(rightSuccNr);
            if (synchronisingActions[rightAction]) {
                continue;
            }
            ensureSuccessorsSize();
            NodeJANI successor = successors[numSuccessors];
            successor.unmark();
            successor.setAndMark(right.getSuccessorNode(rightSuccNr));
            label.set(numSuccessors, rightAction);
            weight.set(numSuccessors, realZero);
            numSuccessors++;
        }
    }

    /**
     * Query for the case that left or right node (not both) is not a state.
     * This corresponds to the case where in one of the components an edge has
     * been chosen which is not synchronised. Thus, we have to explore the
     * possible successors of the resulting transition, while not doing anything
     * else with the part for the other component.
     * The node parameter may not be {@code null}. Note that is the query to
     * the left and right explorers must have been already performed.
     * 
     * @param node node to query
     * @param right whether the right component node is not a state (ow. left)
     */
    private void queryCopy(NodeJANI node, boolean right) {
        assert node != null;
        state.set(false);
        int innerNumSuccessors;
        if (right) {
            innerNumSuccessors = this.right.getNumSuccessors();
        } else {
            innerNumSuccessors = this.left.getNumSuccessors();			
        }
        for (int succNr = 0; succNr < innerNumSuccessors; succNr++) {
            ensureSuccessorsSize();
            NodeJANI successor = successors[numSuccessors];
            successor.unmark();
            if (right) {
                successor.setAndMark(this.right.getSuccessorNode(succNr));
            } else {
                successor.setAndMark(this.left.getSuccessorNode(succNr));				
            }
            Value weight;
            if (right) {
                weight = rightWeight.get(succNr);
            } else {
                weight = leftWeight.get(succNr);
            }
            this.weight.set(succNr, weight);
            label.set(succNr, 0);
            numSuccessors++;
        }
    }

    /**
     * Query for the case that we have to multiply out transition weights.
     * This occurs if we consider edges resulting from synchronising
     * transitions. In this case, we have to consider all possible pairs of
     * left/right successors and multiply their weights.
     * Note that is the query to
     * the left and right explorers must have been already performed.
     * @param nodeParallel 
     * 
     */
    private void queryMultiplyProbabilities(NodeJANI node) {
        state.set(false);
        int leftNumSuccessors = left.getNumSuccessors();
        int rightNumSuccessors = right.getNumSuccessors();
        for (int leftSuccNr = 0; leftSuccNr < leftNumSuccessors; leftSuccNr++) {
            for (int rightSuccNr = 0; rightSuccNr < rightNumSuccessors; rightSuccNr++) {
                ensureSuccessorsSize();
                NodeJANI successor = successors[numSuccessors];
                successor.unmark();
                successor.setAndMark(left.getSuccessorNode(leftSuccNr));
                if (!successor.setSet(right.getSuccessorNode(rightSuccNr))) {
                    fail(ProblemsJANIExplorer.JANI_EXPLORER_GLOBAL_MULTIPLE);
                }

                multiply.apply(weightProduct, leftWeight.get(leftSuccNr), rightWeight.get(rightSuccNr));
                weight.set(numSuccessors, weightProduct);
                label.set(numSuccessors, 0);
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

    @Override
    public void setNumSuccessors(int numSuccessors) {
        this.numSuccessors = numSuccessors;
    }

    @Override
    public boolean isState(NodeJANI node) {
        return left.isState(node) && right.isState(node);
    }

    @Override
    public boolean isState() {
        return state.getBoolean();
    }

    @Override
    public void close() {
    }
}
