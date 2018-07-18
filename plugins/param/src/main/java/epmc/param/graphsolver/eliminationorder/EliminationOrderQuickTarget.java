package epmc.param.graphsolver.eliminationorder;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class EliminationOrderQuickTarget implements EliminationOrder {
    public final static class Builder implements EliminationOrder.Builder {
        private MutableGraph graph;
        private BitSet target;

        @Override
        public Builder setGraph(MutableGraph graph) {
            this.graph = graph;
            return this;
        }

        @Override
        public Builder setTarget(BitSet target) {
            this.target = target;
            return this;
        }

        @Override
        public EliminationOrder build() {
            return new EliminationOrderQuickTarget(this);
        }

    }

    public final static String IDENTIFIER = "quick-target";
    private final BitSet targets;
    private final MutableGraph graph;
    private final BitSet todo = new BitSetUnboundedLongArray();

    private EliminationOrderQuickTarget(Builder builder) {
        this.targets = builder.target;
        this.graph = builder.graph;
        todo.flip(0, graph.getNumNodes());
    }

    @Override
    public boolean hasNodes() {
        return !todo.isEmpty();
    }

    public int nextNodeHmpf() {
        for (int target = targets.nextSetBit(0); target >= 0; target = targets.nextSetBit(target + 1)) {
            if (todo.get(target)) {
                todo.clear(target);
                return target;
            }
        }
        int nodeFound = -1;

        BitSet extendedTargets = new BitSetUnboundedLongArray();
        extendedTargets.or(targets);
        int tries = 0;
        while (nodeFound == -1) {
            for (int target = targets.nextSetBit(0); target >= 0; target = targets.nextSetBit(target + 1)) {
                for (int nodeNr = 0; nodeNr < graph.getNumPredecessors(target); nodeNr++) {
                    int node = graph.getPredecessorNode(target, nodeNr);
                    if (!todo.get(node)) {
                        continue;
                    }
                    boolean ok = true;
                    for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                        int succ = graph.getSuccessorNode(node, succNr);
                        if (!extendedTargets.get(succ) && succ != node) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        nodeFound = node;
                        break;
                    }
                }
            }
            BitSet newExtended = new BitSetUnboundedLongArray();
            newExtended.or(extendedTargets);
            for (int target = extendedTargets.nextSetBit(0); target >= 0; target = extendedTargets.nextSetBit(target + 1)) {
                for (int nodeNr = 0; nodeNr < graph.getNumPredecessors(target); nodeNr++) {
                    int node = graph.getPredecessorNode(target, nodeNr);
                    newExtended.set(node);
                }
            }
            extendedTargets = newExtended;
            tries++;
        }
        todo.clear(nodeFound);
        System.out.println("tries " + tries);
        System.out.println("todo " + todo.cardinality());
        return nodeFound;
    }
    @Override
    public int nextNode() {
        for (int target = targets.nextSetBit(0); target >= 0; target = targets.nextSetBit(target + 1)) {
            if (todo.get(target)) {
                todo.clear(target);
                return target;
            }
        }
        int nodeFound = -1;
        int minNumNonTarget = Integer.MAX_VALUE;
        int minNumNonTargetOrSame = Integer.MAX_VALUE;

        BitSet beforeTarget = new BitSetUnboundedLongArray();
        for (int target = targets.nextSetBit(0); target >= 0; target = targets.nextSetBit(target + 1)) {
            for (int nodeNr = 0; nodeNr < graph.getNumPredecessors(target); nodeNr++) {
                int node = graph.getPredecessorNode(target, nodeNr);
                beforeTarget.set(node);
            }
        }
        for (int target = targets.nextSetBit(0); target >= 0; target = targets.nextSetBit(target + 1)) {
            for (int nodeNr = 0; nodeNr < graph.getNumPredecessors(target); nodeNr++) {
                int node = graph.getPredecessorNode(target, nodeNr);
                if (!todo.get(node)) {
                    continue;
                }
                int numNonTarget = 0;
                int numNonTargetOrSame = 0;
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succ = graph.getSuccessorNode(node, succNr);
                    if (!targets.get(succ) && succ != node) {
                        numNonTarget++;
                    }
                    if (!targets.get(succ) && succ != node && !beforeTarget.get(succ)) {
                        numNonTargetOrSame++;
                    }
                }
                if (numNonTarget == 0) {
                    nodeFound = node;
                    minNumNonTarget = numNonTarget;
                    minNumNonTargetOrSame = numNonTargetOrSame;
                }
            }
        }

        nodeFound = -1;
        if (nodeFound < 0) {
            int min = Integer.MAX_VALUE;
            int chosen = -1;
            for (int node = todo.nextSetBit(0); node >= 0; node = todo.nextSetBit(node + 1)) {
                int numPred = graph.getNumPredecessors(node);
                int numSucc = graph.getNumSuccessors(node);
                if (graph.getSuccessorNumber(node, node) != -1) {
                    numPred--;
                    numSucc--;
                }
                int prod = numSucc * numPred;
                if (prod < min) {
                    min = prod;
                    chosen = node;
                }
            }
            nodeFound = chosen;
//            nodeFound = todo.nextSetBit(0);
        }
        todo.clear(nodeFound);
        System.out.println("minNumNonTarget " + minNumNonTarget + " "
                + minNumNonTargetOrSame);
        System.out.println("todo " + todo.cardinality());
        return nodeFound;
    }

}
