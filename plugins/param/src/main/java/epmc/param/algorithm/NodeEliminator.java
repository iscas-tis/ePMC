package epmc.param.algorithm;

import java.util.ArrayList;

import epmc.graph.CommonProperties;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.param.graph.MutableEdgeProperty;
import epmc.param.graph.MutableGraph;
import epmc.param.graph.MutableNodeProperty;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class NodeEliminator {
    public enum WeighterMethod {
        SELF_LOOP,
        SUM,
    }
    
    public final static class Builder {
        private MutableGraph graph;
        private Iterable<Object> rewards = new ArrayList<>();
        private WeighterMethod weighterMethod = WeighterMethod.SELF_LOOP;

        public Builder setGraph(MutableGraph graph) {
            this.graph = graph;
            return this;
        }
        
        public Builder setRewards(Iterable<Object> rewards) {
            this.rewards = rewards;
            return this;
        }
        
        public Builder setWeigherMethod(WeighterMethod weigherMethod) {
            this.weighterMethod = weigherMethod;
            return this;
        }
        
        public NodeEliminator build() {
            return new NodeEliminator(this);
        }
    }
    
    private final MutableGraph graph;
    private final IntArrayList predecessors = new IntArrayList();
    private final ValueArrayAlgebra predToNodeProbs;
    private final ValueAlgebra predToNodeProb;
    private final Int2IntOpenHashMap predSuccToNumber = new Int2IntOpenHashMap();
    private final MutableEdgeProperty probabilities;
    private final ValueAlgebra one;
    private final ValueAlgebra divideBy;
    private final OperatorEvaluator subtractWeight;
    private final OperatorEvaluator divideWeight;
    private final OperatorEvaluator multiplyWeight;
    private final OperatorEvaluator addWeight;
    private final OperatorEvaluator setWeight;
    private final ValueAlgebra newProb;
    private final ValueAlgebra predToSuccProb;
    private final MutableNodeProperty[] rewardProps;
    private final WeighterMethod weighterMethod;
    private final ValueAlgebra oldProb;

    private NodeEliminator(Builder builder) {
        assert TypeWeightTransition.get().equals(TypeWeight.get());
        weighterMethod = builder.weighterMethod;
        one = TypeWeightTransition.get().newValue();
        one.set(1);
        divideBy = TypeWeightTransition.get().newValue();
        newProb = TypeWeightTransition.get().newValue();
        addWeight = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeightTransition.get(), TypeWeightTransition.get());
        subtractWeight = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeightTransition.get(), TypeWeightTransition.get());
        divideWeight = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeightTransition.get(), TypeWeightTransition.get());
        multiplyWeight = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeightTransition.get(), TypeWeightTransition.get());
        setWeight = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeightTransition.get(), TypeWeightTransition.get());
        predToSuccProb = TypeWeightTransition.get().newValue();
        predToNodeProb = TypeWeightTransition.get().newValue();
        graph = builder.graph;
        probabilities = (MutableEdgeProperty) builder.graph.getEdgeProperty(CommonProperties.WEIGHT);
        rewardProps = buildRewardProps(builder.graph, builder.rewards);
        predToNodeProbs = TypeWeightTransition.get().getTypeArray().newValue();
        oldProb = TypeWeightTransition.get().newValue();
    }
    
    private static MutableNodeProperty[] buildRewardProps(MutableGraph graph, Iterable<Object> rewards) {
        assert graph != null;
        assert rewards != null;
        ArrayList<MutableNodeProperty> result = new ArrayList<>();
        for (Object name : rewards) {
            MutableNodeProperty property = (MutableNodeProperty) graph.getNodeProperty(name);
            result.add(property);
        }
        return result.toArray(new MutableNodeProperty[0]);
    }

    public void eliminate(int node) {
        assert node >= 0 : node;
        assert node < graph.getNumNodes();
        assert graph.isUsedNode(node);
        if (skipEliminate(node)) {
            return;
        }
        removeSelfLoop(node);
        collectPredecessorData(node);
        modifyPredecessors(node);
        graph.clearPredecessorList(node);
    }

    public void removeNode(int node) {
        graph.clearSuccessors(node);
        probabilities.clearSuccessors(node);
        for (MutableNodeProperty reward : rewardProps) {
            reward.set(node, 0);
        }
        graph.removeNode(node);
    }
    
    private void removeSelfLoop(int node) {
        int loopAt = graph.getSuccessorNumber(node, node);
        if (loopAt == -1) {
            return;
        }
        computeDivideByValue(node, loopAt);
        for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
            if (succNr == loopAt) {
                continue;
            }
            Value origProb = probabilities.get(node, succNr);
            divideWeight.apply(newProb, origProb, divideBy);
            probabilities.set(node, succNr, newProb);
        }
        ValueAlgebra loopValue = ValueAlgebra.as(probabilities.get(node, loopAt));
        for (MutableNodeProperty reward : rewardProps) {
            divideWeight.apply(newProb, loopValue, divideBy);
            multiplyWeight.apply(newProb, newProb, reward.get(node));
            addWeight.apply(newProb, reward.get(node), newProb);
            reward.set(node, newProb);
        }
        graph.removeSuccessorNumber(node, loopAt);
        probabilities.removeSuccessorNumber(node, loopAt);
    }

    private void computeDivideByValue(int node, int loopAt) {
        if (weighterMethod == WeighterMethod.SELF_LOOP) {
            ValueAlgebra loopValue = ValueAlgebra.as(probabilities.get(node, loopAt));
            subtractWeight.apply(divideBy, one, loopValue);
        } else if (weighterMethod == WeighterMethod.SUM) {
            divideBy.set(0);
            for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                if (succNr == loopAt) {
                    continue;
                }
                Value probRead = probabilities.get(node, succNr);
                addWeight.apply(divideBy, divideBy, probRead);
            }
        } else {
            assert false;
        }
    }

    private void collectPredecessorData(int node) {
        predecessors.clear();
        predToNodeProbs.setSize(graph.getNumPredecessors(node));
        int usedPredNr = 0;
        for (int predNr = 0; predNr < graph.getNumPredecessors(node); predNr++) {
            int pred = graph.getPredecessorNode(node, predNr);
            if (pred == node) {
                continue;
            }
            int predNodeSuccNr = graph.getSuccessorNumber(pred, node);
            Value predToNodeProb = probabilities.get(pred, predNodeSuccNr);
            predecessors.add(pred);
            predToNodeProbs.set(predToNodeProb, usedPredNr);
            usedPredNr++;
        }
    }

    private void modifyPredecessors(int node) {
        for (int predNr = 0; predNr < predecessors.size(); predNr++) {
            int pred = predecessors.getInt(predNr);
            int predToNodeSuccNr = graph.getSuccessorNumber(pred, node);
            predToNodeProbs.get(predToNodeProb, predNr);
            prepareSuccToNumber(node, pred);
            for (int nodeSuccNr = 0; nodeSuccNr < graph.getNumSuccessors(node); nodeSuccNr++) {
                int succ = graph.getSuccessorNode(node, nodeSuccNr);
                Value nodeToSuccProb = probabilities.get(node, nodeSuccNr);
                multiplyWeight.apply(predToSuccProb, nodeToSuccProb, predToNodeProb);
                if (predSuccToNumber.containsKey(succ)) {
                    int succNr = predSuccToNumber.get(succ);
                    setWeight.apply(oldProb, probabilities.get(pred, succNr));
                    addWeight.apply(newProb, predToSuccProb, oldProb);
                    probabilities.set(pred, succNr, newProb);
                } else {
                    graph.addSuccessor(pred, succ);
//                    predSuccToNumber.put(succ, graph.getNumSuccessors(pred) - 1);
                    probabilities.addSuccessor(pred, predToSuccProb);
                }
            }
            modifyRewardsPredecessor(pred, predToNodeSuccNr, node);
            graph.removeSuccessorNumber(pred, predToNodeSuccNr);
            probabilities.removeSuccessorNumber(pred, predToNodeSuccNr);
        }
    }

    private void modifyRewardsPredecessor(int pred, int predToNodeSuccNr, int node) {
        Value predToNodeProb = probabilities.get(pred, predToNodeSuccNr);
        for (MutableNodeProperty reward : rewardProps) {
            multiplyWeight.apply(newProb, predToNodeProb, reward.get(node));
            addWeight.apply(newProb, reward.get(pred), newProb);
            reward.set(pred, newProb);
        }        
    }

    private void prepareSuccToNumber(int node, int pred) {
        predSuccToNumber.clear();
        int numSuccessors = graph.getNumSuccessors(pred);
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            int succ = graph.getSuccessorNode(pred, succNr);
            if (succ == node) {
                continue;
            }
            predSuccToNumber.put(succ, succNr);
        }
    }

    private boolean skipEliminate(int node) {
        int numSuccessors = graph.getNumSuccessors(node);
        if (numSuccessors == 0 ||
                numSuccessors == 1 &&
                graph.getSuccessorNode(node, 0) == node) {
            return true;
        }
        return false;
    }
}
