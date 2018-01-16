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

package epmc.lumping.lumpingexplicitsignature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.lumping.UtilLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.util.Util;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;

final class LumperExplicitSignature implements LumperExplicit {
    private final static String IDENTIFIER = null;
    private GraphExplicit original;
    private GraphExplicit quotientGraph;
    private GraphSolverObjectiveExplicit quotient;
    private boolean[] blocksSeen;
    private int[] originalToQuotientState;

    private int[] predecessorsFromTo;
    private int[] predecessors;
    private int[] successorsFromTo;
    private int[] successors;
    private ValueArray successorWeights;

    private BlocksTodo todo;
    private Class<? extends Equivalence> equivalenceClass;
    private List<int[]> blocks;
    private RefMultiLocker refMultiLocker = new RefMultiLocker();
    private GraphSolverObjectiveExplicit objective;

    public LumperExplicitSignature(Class<? extends Equivalence> cls) {
        this.equivalenceClass = cls;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean canLump() {
        Equivalence equivalence = Util.getInstance(equivalenceClass);
        equivalence.setObjective(objective);
        return equivalence.canHandle();
    }

    Set<Expression> computeVariables(Expression expression) {
        if (original.getNodeProperties().contains(expression)) {
            return Collections.singleton(expression);
        } else {
            Set<Expression> result = new HashSet<>();
            for (Expression child : expression.getChildren()) {
                result.addAll(computeVariables(child));
            }
            return result;
        }
    }

    private class RefinementRunnable implements Runnable {
        private final Equivalence equivalence;
        private boolean[] blocksSeen;

        RefinementRunnable() {
            this.equivalence = Util.getInstance(equivalenceClass);
            equivalence.setObjective(objective);
            equivalence.setSuccessorsFromTo(successorsFromTo);
            equivalence.setSuccessorStates(successors);
            equivalence.setSuccessorWeights(successorWeights);
            equivalence.setPrecessorsFromTo(predecessorsFromTo);
            equivalence.setPrecessorStates(predecessors);
            equivalence.prepare();
            blocksSeen = new boolean[original.getNumNodes()];
        }

        @Override
        public void run() {
            try {
                int i = 0;
                while (true) {
                    i++;
                    int blockNr = todo.popNext();
                    if (blockNr == BlocksTodo.DONE) {
                        break;
                    }
                    int[] block = blocks.get(blockNr);

                    refMultiLocker.enterRefine();
                    List<int[]> newBlocks = equivalence.splitBlock(block, originalToQuotientState);
                    refMultiLocker.leafRefine();

                    int oldSize = blocks.size();
                    if (newBlocks.size() > 1) {
                        refMultiLocker.enterCommit();
                        commitSplit(blockNr, originalToQuotientState, blocks, newBlocks);
                        refMultiLocker.leafCommit();
                        refMultiLocker.enterAddTodo();
                        addTodosMulti(blocks, originalToQuotientState, blockNr, oldSize);
                        refMultiLocker.leafAddTodo();
                    }
                    todo.done(blockNr);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void addTodosMulti(List<int[]> blocks, int[] partition, int blockNr,
                int oldSize) {
            int blocksSize = blocks.size();
            int newBlocksSize = blocksSize - oldSize + 1;

            for (int j = 0; j < newBlocksSize; j++) {
                int bnr = j == 0 ? blockNr : oldSize + j - 1;
                int[] block = blocks.get(bnr);
                int blockSize = block.length;
                todo.add(bnr);
                for (int i = 0; i < blockSize; i++) {
                    int bst = block[i];
                    int from = predecessorsFromTo[bst];
                    int to = predecessorsFromTo[bst + 1];
                    for (int predNr = from; predNr < to; predNr++) {
                        int predSt = predecessors[predNr];
                        int predBlock = partition[predSt];
                        if (blocksSeen[predBlock]) {
                            todo.add(predBlock);
                        }
                    }
                }
                if (j < newBlocksSize - 1) {
                    for (int i = 0; i < blockSize; i++) {
                        int bst = block[i];
                        int from = predecessorsFromTo[bst];
                        int to = predecessorsFromTo[bst + 1];
                        for (int predNr = from; predNr < to; predNr++) {
                            int predSt = predecessors[predNr];
                            int predBlock = partition[predSt];
                            blocksSeen[predBlock] = true;
                        }
                    }
                }
            }
            for (int j = 0; j < newBlocksSize; j++) {
                int bnr = j == 0 ? blockNr : oldSize + j - 1;
                int[] block = blocks.get(bnr);
                for (int i = 0; i < block.length; i++) {
                    int bst = block[i];
                    int from = predecessorsFromTo[bst];
                    int to = predecessorsFromTo[bst + 1];
                    for (int predNr = from; predNr < to; predNr++) {
                        int predSt = predecessors[predNr];
                        int predBlock = partition[predSt];
                        blocksSeen[predBlock] = false;
                    }
                }
            }
        }
    }

    @Override
    public void lump() {
        this.blocksSeen = new boolean[original.getNumNodes()];
        if (this.originalToQuotientState == null) {
            this.originalToQuotientState = computeInitialPartition();
        }
        computePredecessorsAndMaxFanout();
        Equivalence equivalence = Util.getInstance(equivalenceClass);
        equivalence.setObjective(objective);
        equivalence.setSuccessorsFromTo(successorsFromTo);
        equivalence.setSuccessorStates(successors);
        equivalence.setSuccessorWeights(successorWeights);
        equivalence.setPrecessorsFromTo(predecessorsFromTo);
        equivalence.setPrecessorStates(predecessors);
        equivalence.prepare();
        equivalence.prepareInitialPartition(this.originalToQuotientState);
        this.blocks = computeInitialBlocks(this.originalToQuotientState);
        Comparator<TodoElem> comparator = null;

        comparator = new ComparatorSmallFirst();

//        refinementLoopMultiThread(this, equivalence, comparator);
        refinementLoopSingleThread(this, equivalence, comparator);

        quotientGraph = equivalence.computeQuotient(this.originalToQuotientState, blocks);
        System.out.println("NN " + quotientGraph.getNumNodes());
        if (objective instanceof GraphSolverObjectiveExplicitLump) {
            GraphSolverObjectiveExplicitLump quotientLump = new GraphSolverObjectiveExplicitLump();
            quotientLump.setGraph(quotientGraph);
            quotient = quotientLump;
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability originalUnbounded = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            GraphSolverObjectiveExplicitUnboundedReachability quotientUnbounded = new GraphSolverObjectiveExplicitUnboundedReachability();
            quotientUnbounded.setComputeScheduler(originalUnbounded.isComputeScheduler());
            quotientUnbounded.setGraph(quotientGraph);
            quotientUnbounded.setMin(originalUnbounded.isMin());
            int numQuotientStates = quotientGraph.computeNumStates();
            BitSet origTarget = originalUnbounded.getTarget();
            BitSet origZero = originalUnbounded.getZeroSet();
            BitSet quotTarget = new BitSetBoundedLongArray(numQuotientStates);
            BitSet quotZero = new BitSetBoundedLongArray(numQuotientStates);
            int numOrigStates = original.computeNumStates();
            for (int origState = 0; origState < numOrigStates; origState++) {
                int quotState = originalToQuotientState[origState];
                quotTarget.set(quotState, origTarget.get(origState));
                quotZero.set(quotState, origZero.get(origState));
            }
            quotientUnbounded.setTarget(quotTarget);
            quotientUnbounded.setZeroSink(quotZero);
            quotient = quotientUnbounded;
        } else {
            quotient = null;
            assert false;
        }

        clear();
    }

    private void refinementLoopMultiThread(
            LumperExplicitSignature lumperExplicitSignature,
            Equivalence equivalence, Comparator<TodoElem> comparator) {
        this.todo = new BlocksTodoSynchronised(comparator, blocks, original.getNumNodes());
        int blocksSize = blocks.size();
        for (int i = 0; i < blocksSize; i++) {
            todo.add(i);
        }
        int numThreads = 4;
        Thread[] threads = new Thread[numThreads];
        for (int threadNr = 0; threadNr < numThreads; threadNr++) {
            Thread thread = new Thread(new RefinementRunnable());
            threads[threadNr] = thread;
            thread.start();
        }
        for (int threadNr = 0; threadNr < numThreads; threadNr++) {
            try {
                threads[threadNr].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void refinementLoopSingleThread(LumperExplicitSignature lumper, Equivalence equivalence, Comparator<TodoElem> comparator) {
        this.todo = new BlocksTodoUnsynchronised(comparator, blocks, original.getNumNodes());
        int blocksSize = blocks.size();
        for (int i = 0; i < blocksSize; i++) {
            todo.add(i);
        }

        while (true) {
            int blockNr = todo.popNext();
            if (blockNr == BlocksTodo.DONE) {
                break;
            }
            int[] block = blocks.get(blockNr);

            List<int[]> newBlocks = equivalence.splitBlock(block, this.originalToQuotientState);
            commit(blockNr, newBlocks);
        }
    }

    private void commit(int blockNr, List<int[]> newBlocks) {
        if (newBlocks.size() > 1) {
            int oldSize = blocks.size();
            commitSplit(blockNr, this.originalToQuotientState, blocks, newBlocks);
            addTodos(blocks, this.originalToQuotientState, blockNr, oldSize);
        }
        todo.done(blockNr);
    }

    private final static class RefMultiLocker {
        private final Lock lock = new ReentrantLock();
        private final Condition conditionRefine = lock.newCondition();
        private final Condition conditionCommit = lock.newCondition();
        private final Condition conditionAddTodos = lock.newCondition();
        private boolean committing;
        private int refining;
        private int addTodos;

        void enterRefine() throws InterruptedException {
            lock.lock();
            try {
                while (committing) {
                    while (committing) {
                        conditionCommit.await();
                    }
                }
                refining++;
            } finally {
                lock.unlock();
            }
        }

        void leafRefine() {
            lock.lock();
            try {
                refining--;
                conditionRefine.signal();
            } finally {
                lock.unlock();
            }
        }

        void enterCommit() throws InterruptedException {
            lock.lock();
            try {
                while (committing || refining > 0 || addTodos > 0) {
                    while (committing) {
                        conditionCommit.await();
                    }
                    while (refining > 0) {
                        conditionRefine.await();
                    }
                    while (addTodos > 0) {
                        conditionAddTodos.await();
                    }
                }
                committing = true;
            } finally {
                lock.unlock();
            }
        }

        void leafCommit() {
            lock.lock();
            try {
                committing = false;
                conditionCommit.signalAll();
            } finally {
                lock.unlock();
            }            
        }

        void enterAddTodo() throws InterruptedException {
            lock.lock();
            try {
                while (committing) {
                    conditionCommit.await();
                }
                addTodos++;
            } finally {
                lock.unlock();
            }
        }

        void leafAddTodo() {
            lock.lock();
            try {
                addTodos--;
                conditionAddTodos.signal();
            } finally {
                lock.unlock();
            }            
        }

    }

    private void clear() {
        blocksSeen = null;
    }

    private void computePredecessorsAndMaxFanout() {
        int numStates = original.getNumNodes();
        successorsFromTo = new int[numStates + 1];
        predecessorsFromTo = new int[numStates + 1];
        int totalNumSucc = 0;
        for (int state = 0; state < numStates; state++) {
            int numSucc = original.getNumSuccessors(state);
            for (int succNr = 0; succNr < numSucc; succNr++) {
                int succState = original.getSuccessorNode(state, succNr);
                predecessorsFromTo[succState + 1]++;
            }
            totalNumSucc += numSucc;
            successorsFromTo[state + 1] = successorsFromTo[state] + numSucc;
        }
        successors = new int[totalNumSucc];
        successorWeights = newValueArrayWeight(totalNumSucc);
        for (int state = 0; state < numStates; state++) {
            predecessorsFromTo[state + 1] += predecessorsFromTo[state];
        }
        int totalNumPredecessors = predecessorsFromTo[numStates];
        predecessors = new int[totalNumPredecessors];
        totalNumSucc = 0;
        EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
        for (int state = 0; state < numStates; state++) {
            int numSucc = original.getNumSuccessors(state);
            for (int succNr = 0; succNr < numSucc; succNr++) {
                int succState = original.getSuccessorNode(state, succNr);
                predecessors[predecessorsFromTo[succState]] = state;                
                predecessorsFromTo[succState]++;
                successors[totalNumSucc] = succState;
                successorWeights.set(weights.get(state, succNr), totalNumSucc);
                totalNumSucc++;
            }
        }
        for (int state = numStates; state >= 1; state--) {
            predecessorsFromTo[state] = predecessorsFromTo[state - 1];            
        }
        predecessorsFromTo[0] = 0;
    }

    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private void addTodos(List<int[]> blocks, int[] partition, int blockNr,
            int oldSize) {
        int blocksSize = blocks.size();
        int newBlocksSize = blocksSize - oldSize + 1;

        for (int j = 0; j < newBlocksSize; j++) {
            int bnr = j == 0 ? blockNr : oldSize + j - 1;
            int[] block = blocks.get(bnr);
            int blockSize = block.length;
            todo.add(bnr);
            for (int i = 0; i < blockSize; i++) {
                int bst = block[i];
                int from = predecessorsFromTo[bst];
                int to = predecessorsFromTo[bst + 1];
                for (int predNr = from; predNr < to; predNr++) {
                    int predSt = predecessors[predNr];
                    int predBlock = partition[predSt];
                    if (blocksSeen[predBlock]) {
                        todo.add(predBlock);
                    }
                }
            }
            if (j < newBlocksSize - 1) {
                for (int i = 0; i < blockSize; i++) {
                    int bst = block[i];
                    int from = predecessorsFromTo[bst];
                    int to = predecessorsFromTo[bst + 1];
                    for (int predNr = from; predNr < to; predNr++) {
                        int predSt = predecessors[predNr];
                        int predBlock = partition[predSt];
                        blocksSeen[predBlock] = true;
                    }
                }
            }
        }
        for (int j = 0; j < newBlocksSize; j++) {
            int bnr = j == 0 ? blockNr : oldSize + j - 1;
            int[] block = blocks.get(bnr);
            for (int i = 0; i < block.length; i++) {
                int bst = block[i];
                int from = predecessorsFromTo[bst];
                int to = predecessorsFromTo[bst + 1];
                for (int predNr = from; predNr < to; predNr++) {
                    int predSt = predecessors[predNr];
                    int predBlock = partition[predSt];
                    blocksSeen[predBlock] = false;
                }
            }
        }
    }

    private void commitSplit(int blockNr, int[] partition,
            List<int[]> blocks, List<int[]> newBlocks) {
        Iterator<int[]> iter = newBlocks.iterator();
        int[] remainingBlock = iter.next();
        blocks.set(blockNr, remainingBlock);
        int newNumber = blocks.size();
        while (iter.hasNext()) {
            int[] newBlock = iter.next();
            int newBlockSize = newBlock.length;
            for (int i = 0; i < newBlockSize; i++) {
                int state = newBlock[i];
                partition[state] = newNumber;
            }
            blocks.add(newBlock);
            newNumber++;
        }
    }

    private static List<int[]> computeInitialBlocks(int[] partition) {
        int numBlocks = 0;
        for (int i = 0; i < partition.length; i++) {
            numBlocks = Math.max(numBlocks, partition[i] + 1);
        }
        int[] sizes = new int[numBlocks];
        for (int i = 0; i < partition.length; i++) {
            int blockNr = partition[i];
            sizes[blockNr]++;
        }
        List<int[]> result = new ArrayList<>(numBlocks);
        for (int i = 0; i < numBlocks; i++) {
            result.add(new int[sizes[i]]);
        }
        Arrays.fill(sizes, 0);
        for (int i = 0; i < partition.length; i++) {
            int blockNr = partition[i];
            int[] block = result.get(blockNr);
            block[sizes[blockNr]] = i;
            sizes[blockNr]++;
        }

        return result;
    }

    private int[] computeInitialPartition() {
        if (this.objective instanceof GraphSolverObjectiveExplicitLump) {
            GraphSolverObjectiveExplicitLump objectiveLump = (GraphSolverObjectiveExplicitLump) objective;
            int[] result = new int[objectiveLump.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = objectiveLump.getBlock(i);
            }
            return result;
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability objectiveReach = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            int numStates = objectiveReach.getGraph().computeNumStates();
            int[] result = new int[numStates];
            BitSet targets = objectiveReach.getTarget();
            for (int i = 0; i < numStates; i++) {
                result[i] = targets.get(i) ? 1 : 0;
            }
            UtilLump.fillGaps(result);
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void setOriginal(GraphSolverObjectiveExplicit objective) {
        this.objective = objective;
        original = objective.getGraph();
    }

    @Override
    public GraphSolverObjectiveExplicit getQuotient() {
        return quotient;
    }

    @Override
    public void quotientToOriginal() {
        if (objective instanceof GraphSolverObjectiveExplicitLump) {

        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability unboundedOrig = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            GraphSolverObjectiveExplicitUnboundedReachability unboundedQuot = (GraphSolverObjectiveExplicitUnboundedReachability) quotient;
            int numStatesOrig = original.computeNumStates();
            ValueArray quotResult = unboundedQuot.getResult();
            ValueArray origResult = UtilValue.newArray(quotResult.getType(), numStatesOrig);
            Value entry = unboundedQuot.getResult().getType().getEntryType().newValue();
            for (int origState = 0; origState < numStatesOrig; origState++) {
                int quotState = originalToQuotientState[origState];
                quotResult.get(entry, quotState);
                origResult.set(entry, origState);
            }
            unboundedOrig.setResult(origResult);
        } else {
            assert false;
        }
    }
}
