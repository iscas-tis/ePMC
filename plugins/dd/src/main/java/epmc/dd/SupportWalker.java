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

package epmc.dd;

import java.util.Arrays;
import java.util.Collection;

import epmc.operator.OperatorEq;
import epmc.operator.OperatorIsZero;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class SupportWalker {
    private final static class LongPair {
        private final static String LANGLE = "(";
        private final static String COMMA = ",";
        private final static String RANGLE = ")";

        private long long1;
        private long long2;

        LongPair(long long1, long long2) {
            this.long1 = long1;
            this.long2 = long2;
        }

        void reset(long long1, long long2) {
            this.long1 = long1;
            this.long2 = long2;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = (int)(long1^(long1>>>32)) + (hash << 6) + (hash << 16) - hash;
            hash = (int)(long2^(long2>>>32)) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof LongPair)) {
                return false;
            }
            LongPair other = (LongPair) obj;
            if (long1 != other.long1) {
                return false;
            }
            if (long2 != other.long2) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return LANGLE + long1 + COMMA + long2 + RANGLE;
        }
    }

    private final static String GRAPH_START = "digraph {\n";

    private final static int LEAF_REACHED = Integer.MAX_VALUE;
    private final static int HIGH_ADD = 0;
    private final static int LOW_ADD = 1;
    private final static int NUM_OUT = 2;
    private final int[] variables;
    private final int[] goBackStack;
    private int index;
    private int[] diagram;
    private Value[] leafValues;
    private int variable;
    private Value[] stopWhere;
    private ContextDD contextDD;
    private int trueIndex = Integer.MIN_VALUE;
    private int falseIndex = Integer.MIN_VALUE;
    private int zeroIndex = Integer.MIN_VALUE;
    private final OperatorEvaluator eq;
    private final ValueBoolean cmp;

    // TODO extend stop-at

    // copy constructor
    private SupportWalker(int[] variables, int[] goBackStack, int index,
            int[] diagram, Value[] leafValues, int variable,
            Value[] stopWhere, ContextDD contextDD,
            int trueIndex, int falseIndex, int zeroIndex) {
        // note: copy only as necessary. take care of data structures change
        this.variables = variables;
        this.goBackStack = Arrays.copyOf(goBackStack, goBackStack.length);
        this.index = index;
        this.diagram = diagram;
        this.leafValues = leafValues;
        this.variable = variable;
        this.stopWhere = stopWhere;
        this.contextDD = contextDD;
        this.trueIndex = trueIndex;
        this.falseIndex = falseIndex;
        this.zeroIndex = zeroIndex;
        eq = ContextValue.get().getEvaluator(OperatorEq.EQ, leafValues[0].getType(), leafValues[0].getType());
        cmp = TypeBoolean.get().newValue();
    }

    SupportWalker(DD node, DD support, Value[] stopWhere) {
        assert node != null;
        assert support != null;
        assert support.assertCube();
        assert node.getContext() == support.getContext();
        assert assertSupport(node, support);
        assert stopWhere != null;
        this.contextDD = node.getContext();
        this.stopWhere = new Value[stopWhere.length];
        for (int index = 0; index < stopWhere.length; index++) {
            this.stopWhere[index] = UtilValue.clone(stopWhere[index]);
        }
        this.variables = computeVariables(support);
        goBackStack = new int[variables.length];
        eq = ContextValue.get().getEvaluator(OperatorEq.EQ, node.getType(), node.getType());
        cmp = TypeBoolean.get().newValue();
        buildDiagram(node, support);
    }

    SupportWalker(DD node, DD support, Collection<Value> stopWhere) {
        this(node, support, stopWhere.toArray(new Value[0]));
    }

    SupportWalker(DD node, DD support) {
        this(node, support, true, true);
    }

    SupportWalker(DD node, DD support, boolean stopAtFalse, boolean stopAtZero) {
        this(node, support, toStopWhere(stopAtFalse, stopAtZero, node));
    }

    private static Value[] toStopWhere(boolean stopAtFalse,
            boolean stopAtZero, DD node) {
        assert node != null;
        Value[] stopWhere = new Value[(stopAtFalse ? 1 : 0) + (stopAtZero ? 1 : 0)];
        int index = 0;
        if (stopAtFalse) {
            stopWhere[index] = UtilValue.newValue(TypeBoolean.get(), false);
            index++;
        }
        if (stopAtZero) {
            stopWhere[index] = UtilValue.newValue(TypeInteger.get(), 0);
        }
        return stopWhere;
    }

    private static boolean assertSupport(DD node, DD support) {
        IntOpenHashSet nodeSupport = node.support();
        IntOpenHashSet supportSupport = support.support();
        nodeSupport.forEach((int variable) -> {
            assert supportSupport.contains(variable);
        });
        return true;
    }

    private static int[] computeVariables(DD support) {
        int[] variables;
        Walker supportWalker = support.walker();
        int numVariables = 0;
        while (!supportWalker.isTrue()) {
            supportWalker.high();
            numVariables++;
        }
        variables = new int[numVariables];
        supportWalker = support.walker();
        int variableNr = 0;
        while (!supportWalker.isTrue()) {
            variables[variableNr] = supportWalker.variable();
            supportWalker.high();
            variableNr++;
        }

        return variables;
    }

    private void buildDiagram(DD node, DD support) {
        Walker nodeWalker = node.walker();
        Walker supportWalker = support.walker();
        Object2IntOpenHashMap<Value> leafEnumerator = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<LongPair> nodeEnumerator = new Object2IntOpenHashMap<>();
        enumerateNodes(nodeWalker, supportWalker, nodeEnumerator, leafEnumerator);
        int totalNumNodes = nodeEnumerator.values().size();
        diagram = new int[totalNumNodes * NUM_OUT];
        Object2IntOpenHashMap<Value> valueEnumerator = new Object2IntOpenHashMap<>();
        int[] valueNumber = new int[1];
        leafValues = new Value[leafEnumerator.size()];
        leafEnumerator.forEach((a,b) -> {
            Value value = UtilValue.clone(a);
            valueEnumerator.put(value, valueNumber[0]);
            leafValues[valueNumber[0]] = value;
            valueNumber[0]++;
        });
        BitSet seen = UtilBitSet.newBitSetUnbounded();
        buildDiagram(nodeWalker, supportWalker, nodeEnumerator, valueEnumerator, seen);
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator isZero = ContextValue.get().getEvaluatorOrNull(OperatorIsZero.IS_ZERO, leafValues[0].getType());
        for (int i = 0; i < diagram.length / 2; i++) {
            if (diagram[i * NUM_OUT] >= 0) {
                continue;
            }
            Value value = leafValues[-diagram[i * NUM_OUT] - 1];
            if (isZero != null) {
                isZero.apply(cmp, value);
            } else {
                cmp.set(false);
            }
            if (ValueBoolean.isTrue(value)) {
                assert trueIndex == Integer.MIN_VALUE;
                trueIndex = i;
            } else if (ValueBoolean.isFalse(value)) {
                assert falseIndex == Integer.MIN_VALUE;
                falseIndex = i;
            } else if (cmp.getBoolean()) {
                assert zeroIndex == Integer.MIN_VALUE;
                zeroIndex = i;
            }
        }
    }

    private void buildDiagram(Walker node, Walker support,
            Object2IntOpenHashMap<LongPair> nodeEnumerator,
            Object2IntOpenHashMap<Value> valueEnumerator, BitSet seen) {
        LongPair pair = new LongPair(node.uniqueId(), support.uniqueId());
        assert nodeEnumerator.containsKey(pair);
        int index = nodeEnumerator.getInt(pair);
        if (seen.get(index)) {
            return;
        }
        seen.set(index);

        boolean stopAt = stopHere(node);
        if (stopAt || support.isLeaf()) {
            assert valueEnumerator.containsKey(node.value());
            int value = valueEnumerator.getInt(node.value());
            diagram[index * NUM_OUT] = -(value + 1);
        } else {
            int nodeVar = node.isLeaf() ? LEAF_REACHED : node.variable();
            int supportVar = support.isLeaf() ? LEAF_REACHED : support.variable();
            boolean doStep = nodeVar == supportVar;
            support.high();
            if (doStep) {
                node.high();
                pair.reset(node.uniqueId(), support.uniqueId());
                int indexHigh = nodeEnumerator.getInt(pair);
                buildDiagram(node, support, nodeEnumerator, valueEnumerator, seen);
                node.back();
                node.low();
                pair.reset(node.uniqueId(), support.uniqueId());
                int indexLow = nodeEnumerator.getInt(pair);
                buildDiagram(node, support, nodeEnumerator, valueEnumerator, seen);
                node.back();
                diagram[index * NUM_OUT + HIGH_ADD] = indexHigh;
                diagram[index * NUM_OUT + LOW_ADD] = indexLow;
            } else {
                pair.reset(node.uniqueId(), support.uniqueId());
                int indexHigh = nodeEnumerator.getInt(pair);
                int indexLow = nodeEnumerator.getInt(pair);
                diagram[index * NUM_OUT + HIGH_ADD] = indexHigh;
                diagram[index * NUM_OUT + LOW_ADD] = indexLow;
                buildDiagram(node, support, nodeEnumerator, valueEnumerator, seen);
            }
            support.back();
        }
    }

    private boolean stopHere(Walker node) {
        boolean stopAt = false;
        if (node.isLeaf()) {
            Value nodeValue = node.value();
            for (Value stop : stopWhere) {
                // TODO check following, probably very inefficient
                if (UtilValue.upper(stop.getType(), nodeValue.getType()) != null) {
                    eq.apply(cmp, stop, nodeValue);
                    if (cmp.getBoolean()) {
                        stopAt = true;
                        break;
                    }
                }
            }
        }
        return stopAt;
    }

    private void enumerateNodes(Walker node, Walker support,
            Object2IntOpenHashMap<LongPair> nodeEnumerator,
            Object2IntOpenHashMap<Value> leafEnumerator) {
        LongPair pair = new LongPair(node.uniqueId(), support.uniqueId());
        if (nodeEnumerator.containsKey(pair)) {
            return;
        }
        boolean stopAt = stopHere(node);

        if (stopAt || support.isLeaf()) {
            int index;
            if (!leafEnumerator.containsKey(node.value())) {
                index = nodeEnumerator.size();
                leafEnumerator.put(node.value(), index);
            } else {
                index = leafEnumerator.getInt(node.value());
            }
            nodeEnumerator.put(pair, index);
        } else {
            int index = nodeEnumerator.size();
            nodeEnumerator.put(pair, index);
            int nodeVar = node.isLeaf() ? LEAF_REACHED : node.variable();
            int supportVar = support.isLeaf() ? LEAF_REACHED : support.variable();
            boolean doStep = nodeVar == supportVar;
            support.high();
            if (doStep) {
                node.high();
                enumerateNodes(node, support, nodeEnumerator, leafEnumerator);
                node.back();
                node.low();
                enumerateNodes(node, support, nodeEnumerator, leafEnumerator);
                node.back();
            } else {
                enumerateNodes(node, support, nodeEnumerator, leafEnumerator);
            }
            support.back();
        }
    }

    public int low(int index) {
        assert diagram[index * NUM_OUT] >= 0;
        return diagram[index * NUM_OUT + LOW_ADD];
    }

    public int high(int index) {
        assert diagram[index * NUM_OUT] >= 0;
        return diagram[index * NUM_OUT + HIGH_ADD];
    }

    public void low() {
        assert diagram[index * NUM_OUT] >= 0;
        goBackStack[variable] = index;
        variable++;
        index = diagram[index * NUM_OUT + LOW_ADD];
    }

    public void high() {
        assert diagram[index * NUM_OUT] >= 0;
        goBackStack[variable] = index;
        variable++;
        index = diagram[index * NUM_OUT + HIGH_ADD];
    }

    public void back() {
        assert index > 0;
        variable--;
        index = goBackStack[variable];
    }

    public int variable(int number) {
        return variables[number];
    }

    public int variable() {
        assert index >= 0;
        assert index * NUM_OUT < diagram.length;
        return isLeaf(index) ? LEAF_REACHED : variable(variable);
    }


    public boolean isLeaf(int index) {
        return diagram[index * NUM_OUT] < 0;
    }

    public boolean isLeaf() {
        return isLeaf(index);
    }

    public Value value(int index) {
        assert isLeaf(index);
        return leafValues[-diagram[index * NUM_OUT] - 1];
    }

    public Value value() {
        return value(index);
    }

    public boolean isFalse() {
        assert(index == falseIndex) == (isLeaf() && ValueBoolean.isFalse(value()));
        return index == falseIndex;
    }

    public boolean isTrue() {
        assert(index == trueIndex) == (isLeaf() && ValueBoolean.isTrue(value()));
        return index == trueIndex;
    }

    public boolean isZero() {
        return index == zeroIndex;
    }

    public long uniqueId() {
        return index;
    }

    public SupportWalkerNodeMap newNodeMap(Type type) {
        assert type != null;
        return new SupportWalkerNodeMap(this, type);
    }

    public SupportWalkerNodeMapInt newNodeMapInt() {
        return new SupportWalkerNodeMapInt(this);
    }

    int getIndex() {
        return index;
    }

    int getNumNodes() {
        return diagram.length;
    }

    ContextDD getContext() {
        return contextDD;
    }


    public SupportWalker permute(Permutation permutation) {
        assert permutation != null;
        assert permutation.getContextDD() == contextDD;
        int[] newVariables = new int[variables.length];
        for (int i = 0; i < variables.length; i++) {
            newVariables[i] = permutation.getPermuted(variables[i]);
        }
        return new SupportWalker(newVariables, goBackStack, index, diagram,
                leafValues, variable, stopWhere, contextDD,
                trueIndex, falseIndex, zeroIndex);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(GRAPH_START);

        int[] map = new int[diagram.length / NUM_OUT];
        Arrays.fill(map, -1);
        computeVarMap(0, 0, map);

        for (int node = 0; node < diagram.length / NUM_OUT; node++) {
            int variable = map[node];
            String label = variable == LEAF_REACHED
                    ? value(node) + ": " + value(node).getType()
                            : String.valueOf(variable);
                    String shape = variable == LEAF_REACHED ? "box" : "circle";
                    builder.append("  node" + node + " [label=\"" + label);
                    builder.append("\", shape=\"" + shape + "\"];\n");
        }
        builder.append("\n");
        BitSet seen = UtilBitSet.newBitSetUnbounded();
        transitionsToString(builder, seen, 0);
        builder.append("}\n");
        return builder.toString();
    }

    private void computeVarMap(int node, int varNumber, int[] map) {
        if (map[node] != -1) {
            return;
        }
        if (isLeaf(node)) {
            map[node] = LEAF_REACHED;
            return;
        }
        map[node] = variable(varNumber);
        computeVarMap(low(node), varNumber + 1, map);
        computeVarMap(high(node), varNumber + 1, map);
    }

    private void transitionsToString(StringBuilder builder, BitSet seen, int node) {
        if (seen.get(node)) {
            return;
        }
        seen.set(node);
        if (isLeaf(node)) {
            return;
        }
        int low = low(node);
        int high = high(node);
        builder.append("  node" + node + " -> node" + low + " ");
        builder.append("[style=dashed,arrowhead=none];\n");
        builder.append("  node" + node + " -> node" + high + " ");
        builder.append("[style=solid,arrowhead=none];\n");
        transitionsToString(builder, seen, low);
        transitionsToString(builder, seen, high);
    }    
}
