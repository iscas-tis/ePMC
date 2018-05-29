package epmc.imdp.robot;

import java.util.Arrays;

import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.RewardSpecificationImpl;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.GraphExporterDOT;
import epmc.graph.explicit.NodeProperty;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeObject;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

final class GraphExplicitIMDPRobot implements GraphExplicit {
    private final class NodePropertyState implements NodeProperty {
        private ValueBoolean value = TypeBoolean.get().newValue();

        @Override
        public GraphExplicit getGraph() {
            return GraphExplicitIMDPRobot.this;
        }

        @Override
        public Value get(int queriedNode) {
            value.set(queriedNode < numStates);
            return value;
        }

        @Override
        public void set(int node, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return TypeBoolean.get();
        }
    }

    private final class NodePropertyPlayer implements NodeProperty {
        private ValueEnum value = TypeEnum.get(Player.class).newValue();

        @Override
        public GraphExplicit getGraph() {
            return GraphExplicitIMDPRobot.this;
        }

        @Override
        public Value get(int queriedNode) {
            value.set(queriedNode < numStates ? Player.ONE : Player.STOCHASTIC);
            return value;
        }

        @Override
        public void set(int node, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return value.getType();
        }
    }

    private final class NodePropertyZero implements NodeProperty {
        Value zero = UtilValue.newValue(TypeWeight.get(), 0);
        
        @Override
        public GraphExplicit getGraph() {
            return GraphExplicitIMDPRobot.this;
        }

        @Override
        public Value get(int queriedNode) {
            return zero;
        }

        @Override
        public void set(int node, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return TypeWeight.get();
        }
    }

    private final class EdgePropertyIMDPRobot implements EdgeProperty {
        private final ValueArrayAlgebra array;
        private final TypeAlgebra typeEntry;
        private final ValueAlgebra zero;
        private final ValueAlgebra weight;

        private EdgePropertyIMDPRobot(ValueArrayAlgebra array) {
            assert array != null;
            TypeArrayAlgebra typeArray = array.getType();
            typeEntry = typeArray.getEntryType();
            this.array = array;
            zero = UtilValue.newValue(typeEntry, 0);
            weight = typeEntry.newValue();
        }

        @Override
        public GraphExplicit getGraph() {
            return GraphExplicitIMDPRobot.this;
        }

        @Override
        public Value get(int queriedNode, int successor) {
            if (queriedNode < numStates) {
                return zero;
            } else {
                int nondet = queriedNode - numStates;
                array.get(weight, nondetFromTo[nondet] + successor);
                return weight;
            }
        }

        @Override
        public void set(int node, int successor, Value value) {
            assert false;
        }

        @Override
        public Type getType() {
            return typeEntry;
        }		
    }

    private final BitSet initial = new BitSetUnboundedLongArray();
    private final GraphExplicitProperties properties;
    private int lastFrom = -1;
    private int lastAction = -1;
    private int numStates;
    private int numNonDet;
    private int numProb;
    private int[] stateFromTo = new int[1];
    private int[] nondetFromTo = new int[1];
    private int[] targets = new int[1];
    private ValueArrayAlgebra weights;
    private ValueArrayAlgebra rewards1;
    private ValueArrayAlgebra rewards2;	

    GraphExplicitIMDPRobot() {
        int initState = Options.get().getInteger(OptionsRobot.IMDP_ROBOT_INITIAL_STATE);
        this.initial.set(initState - 1);
        properties = new GraphExplicitProperties(this);
        NodePropertyState stateProp = new NodePropertyState();
        properties.registerNodeProperty(CommonProperties.STATE, stateProp);
        NodePropertyPlayer playerProp = new NodePropertyPlayer();
        properties.registerNodeProperty(CommonProperties.PLAYER, playerProp);
        weights = UtilValue.newArray(TypeWeightTransition.get().getTypeArray(), 1);
        rewards1 = UtilValue.newArray(TypeWeight.get().getTypeArray(), 1);
        rewards2 = UtilValue.newArray(TypeWeight.get().getTypeArray(), 1);
    }

    void addLine(Line line) {
        assert line != null;
        if (lastFrom != line.getFrom()) {
            assert line.getFrom() == lastFrom + 1;
            assert line.getAction() == 0;
            stateFromTo = ensureSize(stateFromTo, numStates + 1);
            stateFromTo[numStates + 1] = stateFromTo[numStates] + 1;
            nondetFromTo = ensureSize(nondetFromTo, numNonDet + 1);
            nondetFromTo[numNonDet + 1] = nondetFromTo[numNonDet] + 1;
            targets = ensureSize(targets, numProb);
            targets[numProb] = line.getTo();
            weights = UtilValue.ensureSize(weights, numProb + 1);
            weights.set(line.getInterval(), numProb);
            rewards1 = UtilValue.ensureSize(rewards1, numProb + 1);
            rewards1.set(line.getReward1(), numProb);
            rewards2 = UtilValue.ensureSize(rewards2, numProb + 1);
            rewards2.set(line.getReward2(), numProb);
            numStates++;
            numNonDet++;
            numProb++;
            lastFrom = line.getFrom();
            lastAction = line.getAction();
        } else if (lastAction != line.getAction()) {
            stateFromTo = ensureSize(stateFromTo, numStates);
            stateFromTo[numStates]++;
            nondetFromTo = ensureSize(nondetFromTo, numNonDet + 1);
            nondetFromTo[numNonDet + 1] = nondetFromTo[numNonDet] + 1;
            targets = ensureSize(targets, numProb);
            targets[numProb] = line.getTo();
            weights = UtilValue.ensureSize(weights, numProb + 1);
            weights.set(line.getInterval(), numProb);
            rewards1 = UtilValue.ensureSize(rewards1, numProb + 1);
            rewards1.set(line.getReward1(), numProb);
            rewards2 = UtilValue.ensureSize(rewards2, numProb + 1);
            rewards2.set(line.getReward2(), numProb);
            numNonDet++;
            numProb++;
            lastAction = line.getAction();
        } else {
            nondetFromTo = ensureSize(nondetFromTo, numNonDet);
            nondetFromTo[numNonDet]++;
            targets = ensureSize(targets, numProb);
            targets[numProb] = line.getTo();
            weights = UtilValue.ensureSize(weights, numProb + 1);
            weights.set(line.getInterval(), numProb);
            rewards1 = UtilValue.ensureSize(rewards1, numProb + 1);
            rewards1.set(line.getReward1(), numProb);
            rewards2 = UtilValue.ensureSize(rewards2, numProb + 1);
            rewards2.set(line.getReward2(), numProb);
            numProb++;
        }
    }

    private int[] ensureSize(int[] array, int index) {
        if (array.length <= index) {
            array = Arrays.copyOf(array, array.length * 2);
        }
        return array;
    }

    @Override
    public int getNumNodes() {
        return numStates + numNonDet;
    }

    @Override
    public BitSet getInitialNodes() {
        return initial;
    }

    @Override
    public int getNumSuccessors(int queriedNode) {
        if (queriedNode < numStates) {
            return stateFromTo[queriedNode + 1] - stateFromTo[queriedNode];
        } else {
            int nondet = queriedNode - numStates;
            return nondetFromTo[nondet + 1] - nondetFromTo[nondet];
        }
    }

    @Override
    public int getSuccessorNode(int queriedNode, int successor) {
        if (queriedNode < numStates) {
            return numStates + stateFromTo[queriedNode] + successor;
        } else {
            int nondet = queriedNode - numStates;
            return targets[nondetFromTo[nondet] + successor];
        }
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return GraphExporterDOT.toString(this);
    }

    @Override
    public void close() {
    }

    public void done() {
        EdgePropertyIMDPRobot weightProp = new EdgePropertyIMDPRobot(weights);
        properties.registerEdgeProperty(CommonProperties.WEIGHT, weightProp);
        EdgePropertyIMDPRobot reward1Prop = new EdgePropertyIMDPRobot(rewards1);
        RewardSpecificationImpl reward1Id = new RewardSpecificationImpl(new ExpressionLiteral.Builder()
                .setValue("1")
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build());
        properties.registerEdgeProperty(reward1Id, reward1Prop);
        EdgePropertyIMDPRobot reward2Prop = new EdgePropertyIMDPRobot(rewards2);
        RewardSpecificationImpl reward2Id = new RewardSpecificationImpl(new ExpressionLiteral.Builder()
                .setValue("2")
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build());
        properties.registerEdgeProperty(reward2Id, reward2Prop);
        properties.registerNodeProperty(reward1Id, new NodePropertyZero());
        properties.registerNodeProperty(reward2Id, new NodePropertyZero());
        ValueObject valueSemantics = new TypeObject.Builder().setClazz(Semantics.class).build().newValue(SemanticsIMDP.IMDP);
        properties.registerGraphProperty(CommonProperties.SEMANTICS, valueSemantics);
    }
}
