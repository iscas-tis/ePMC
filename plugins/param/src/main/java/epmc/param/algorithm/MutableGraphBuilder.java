package epmc.param.algorithm;

import java.util.ArrayList;

import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDTMC;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorSet;
import epmc.param.graph.MutableEdgeProperty;
import epmc.param.graph.MutableGraph;
import epmc.param.graph.MutableNodeProperty;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedIntArray;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class MutableGraphBuilder {
    public enum PropertyNames {
        TIME
    }
    
    @FunctionalInterface
    public interface NodeMap {
        int map(int node);
    }
    
    private final static class Rewards {
        private final EdgeProperty[] origEdgeRewards;
        private final MutableNodeProperty[] mutableRewards;
        private final ValueArrayAlgebra[] origArrayRewards;
        private final MutableNodeProperty[] mutableArrayRewards;
        private final MutableNodeProperty time;

        private Rewards(EdgeProperty[] origEdgeRewards,
                MutableNodeProperty[] mutableRewards,
                ValueArrayAlgebra[] origArrayRewards,
                MutableNodeProperty[] mutableArrayRewards,
                MutableNodeProperty time) {
            this.origEdgeRewards = origEdgeRewards;
            this.mutableRewards = mutableRewards;
            this.origArrayRewards = origArrayRewards;
            this.mutableArrayRewards = mutableArrayRewards;
            this.time = time;
        }
    }

    private boolean built;
    private GraphExplicit original;
    private Iterable<Object> rewardNames;
    private NodeMap successorMap;
    private boolean addTime;
    private BitSet sinks;
    private ArrayList<ValueArrayAlgebra> rewardArrays = new ArrayList<>();
    private boolean continuousTime;
    
    public MutableGraphBuilder setOriginalGraph(GraphExplicit original) {
        assert !built;
        this.original = original;
        return this;
    }
    
    public MutableGraphBuilder setRewardNames(Iterable<Object> rewardNames) {
        assert !built;
        this.rewardNames = rewardNames;
        return this;
    }
    
    public MutableGraphBuilder addRewardArray(ValueArrayAlgebra reward) {
        assert !built;
        assert reward != null;
        rewardArrays.add(reward);
        return this;
    }

    public MutableGraphBuilder setSuccessorMap(NodeMap successorMap) {
        assert !built;
        this.successorMap = successorMap;
        return this;
    }

    public MutableGraphBuilder setAddTime(boolean addTime) {
        this.addTime = addTime;
        return this;
    }
    
    public MutableGraphBuilder setSinks(BitSet sinks) {
        this.sinks = sinks;
        return this;
    }

    public MutableGraph build() {
        assert !built;
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        assert semantics != null;
        assert SemanticsDTMC.isDTMC(semantics) || SemanticsCTMC.isCTMC(semantics);
        MutableGraph graph = new MutableGraph();
        Rewards rewards = prepareRewards(original, graph, rewardNames, rewardArrays);
        built = true;
        continuousTime = SemanticsContinuousTime.isContinuousTime(semantics);
        constructMutableGraph(graph, original, rewards);
        return graph;
    }
    
    private Rewards prepareRewards(GraphExplicit original, MutableGraph graph, Iterable<Object> names, ArrayList<ValueArrayAlgebra> rewardArrays) {
        if (names == null) {
            names = new ArrayList<>();
        }
        ArrayList<EdgeProperty> origRewards = new ArrayList<>();
        ArrayList<MutableNodeProperty> mutableRewards = new ArrayList<>();
        for (Object name : names) {
            EdgeProperty origReward = original.getEdgeProperty(name);
            MutableNodeProperty mutableReward = graph.addMutableNodeProperty(name, origReward.getType());
            origRewards.add(origReward);
            mutableRewards.add(mutableReward);
        }
        ArrayList<MutableNodeProperty> mutableArrayRewards = new ArrayList<>();
        for (ValueArrayAlgebra array : rewardArrays) {
            MutableNodeProperty mutableReward = graph.addMutableNodeProperty(array, array.getType().getEntryType());
            mutableArrayRewards.add(mutableReward);
            Value value = array.getType().getEntryType().newValue();
            for (int node = 0; node < original.getNumNodes(); node++) {
                array.get(value, node);
                mutableReward.addNode();
                mutableReward.set(node, value);
            }
        }
        MutableNodeProperty time = null;
        if (addTime) {
            time = graph.addMutableNodeProperty(PropertyNames.TIME, TypeWeight.get());
            ValueAlgebra value = ValueAlgebra.as(time.getType().newValue());
            value.set(1);
            for (int node = 0; node < original.getNumNodes(); node++) {
                time.addNode();
                time.set(node, value);
            }
        }
        return new Rewards(origRewards.toArray(new EdgeProperty[0]),
                mutableRewards.toArray(new MutableNodeProperty[0]),
                rewardArrays.toArray(new ValueArrayAlgebra[0]),
                mutableArrayRewards.toArray(new MutableNodeProperty[0]),
                time);
    }

    
    private void constructMutableGraph(MutableGraph graph, GraphExplicit original, Rewards rewards) {
        assert graph != null;
        assert original != null;
        graph.getInitialNodes().or(original.getInitialNodes());
        if (successorMap == null) {
            successorMap = (int a) -> a;
        }
        if (sinks == null) {
            sinks = new BitSetUnboundedIntArray();
        }
        MutableNodeProperty[] mutableRewards = rewards.mutableRewards;
        EdgeProperty origProbabilities = original.getEdgeProperty(CommonProperties.WEIGHT);
        MutableEdgeProperty mutableProbabilities = graph.addMutableEdgeProperty(CommonProperties.WEIGHT,
                TypeWeightTransition.get());
        Int2IntOpenHashMap succToSuccNr = new Int2IntOpenHashMap();
        ValueAlgebra newProb = TypeWeightTransition.get().newValue();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        for (int node = 0; node < original.getNumNodes(); node++) {
            graph.addNode();
            mutableProbabilities.addNode();
            for (int rewardNr = 0; rewardNr < mutableRewards.length; rewardNr++) {
                mutableRewards[rewardNr].addNode();
            }
        }
        ValueAlgebra totalSum = continuousTime ? TypeWeightTransition.get().newValue() : null;
        for (int node = 0; node < original.getNumNodes(); node++) {
            if (sinks.get(node)) {
                int succ = successorMap.map(node);
                newProb.set(1);
                graph.addSuccessor(node, succ);
                mutableProbabilities.addSuccessor(node, newProb);
                for (int rewardNr = 0; rewardNr < mutableRewards.length; rewardNr++) {
                    newProb.set(0);
                    mutableRewards[rewardNr].set(node, newProb);
                }
                continue;
            }
            succToSuccNr.clear();
            if (continuousTime) {
                totalSum.set(0);
                for (int succNr = 0; succNr < original.getNumSuccessors(node); succNr++) {
                    Value origProb = origProbabilities.get(node, succNr);
                    add.apply(totalSum, totalSum, origProb);
                }
            }
            for (int succNr = 0; succNr < original.getNumSuccessors(node); succNr++) {
                int succ = successorMap.map(original.getSuccessorNode(node, succNr));
                Value origProb = origProbabilities.get(node, succNr);
                if (continuousTime) {
                    divide.apply(newProb, origProb, totalSum);                        
                } else {
                    set.apply(newProb, origProb);
                }
                if (succToSuccNr.containsKey(succ)) {
                    Value oldProb = mutableProbabilities.get(node, succToSuccNr.get(succ));
                    add.apply(newProb, oldProb, newProb);
                    int oldSucc = succToSuccNr.get(succ);
                    mutableProbabilities.set(node, oldSucc, newProb);
                } else {
                    graph.addSuccessor(node, succ);
                    succToSuccNr.put(succ, graph.getNumSuccessors(node) - 1);
                    mutableProbabilities.addSuccessor(node, newProb);
                }
            }
            buildRewards(node, totalSum, rewards);
        }
    }

    private void buildRewards(int node, ValueAlgebra totalSum, Rewards rewards) {
        ValueAlgebra v = TypeWeightTransition.get().newValue();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE,
                TypeWeightTransition.get(), TypeWeightTransition.get());
        if (continuousTime) {
            for (int index = 0; index < rewards.mutableArrayRewards.length; index++) {
                MutableNodeProperty struct = rewards.mutableArrayRewards[index];
                Value val = struct.get(node);
                divide.apply(v, val, totalSum);
                struct.set(node, v);
            }
        }
        if (continuousTime && rewards.time != null) {
            Value val = rewards.time.get(node);
            divide.apply(v, val, totalSum);
            rewards.time.set(node, v);
        }
        // TODO finish this method
        
    }

}
