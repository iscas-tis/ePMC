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

package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.coalition.options.OptionsCoalition.JurdzinskyLiftOrder;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.IntDeque;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;

public final class SolverNonStochasticJurdzinski implements SolverNonStochastic {
    public final static String IDENTIFIER = "jurdzinski";
    /** Whether to compute a strategy for the even player. */
    private boolean computeStrategyP0;
    /** Whether to compute a strategy for the odd player. */
    private boolean computeStrategyP1;

    private GraphExplicit game;
    private NodeProperty player;
    private int maxPriority;

    @Override
    public void setGame(GraphExplicit game) {
        assert game != null;
        this.game = game;
        this.player = game.getNodeProperty(CommonProperties.PLAYER);
    }

    @Override
    public void setComputeStrategies(boolean playerEven, boolean playerOdd) {
        this.computeStrategyP0 = playerEven;
        this.computeStrategyP1 = playerOdd;
    }

    @Override
    public QualitativeResult solve() {
        assert game != null;
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_START);
        StopWatch watch = new StopWatch(true);
        int numNodes = this.game.getNumNodes();
        this.maxPriority = computeMaxPriority();
        int vectorSize = (maxPriority + 1) / 2;
        int[] counterBounds = computeCounterBounds();
        int[] vectors = new int[numNodes * vectorSize];
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        /* buffer for previous values, to avoid reallocating it repeatedly */
        int[] previousValue = new int[vectorSize];

        OptionsCoalition.JurdzinskyChooseLiftNodes liftMethod = 
                Options.get().getEnum(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES);
        switch (liftMethod) {
        case ALL:
            liftSuccessorAll(vectors, vectorSize, counterBounds, labels, previousValue);
            break;
        case SUCCESSOR_CHANCED:
            liftSuccessorChanged(vectors, vectorSize, counterBounds, labels, previousValue);
            break;
        default:
            assert false;
            break;		
        }
        SchedulerSimpleSettable strategies = computeStrategyP0 || computeStrategyP1
                ? new SchedulerSimpleArray(game) : null;
                BitSet evenWins = UtilBitSet.newBitSetBounded(game.getNumNodes());
                BitSet oddWins = UtilBitSet.newBitSetBounded(game.getNumNodes());
                for (int node = 0; node < numNodes; node++) {
                    Player player = this.player.getEnum(node);
                    if (computeStrategyP0 && player == Player.ONE) {
                        strategies.set(node, computeDecision(node, vectors, vectorSize, previousValue));
                    } else if (computeStrategyP1 && player == Player.TWO) {
                        strategies.set(node, computeDecision(node, vectors, vectorSize, previousValue));
                    }
                    if (isVectorMaxElem(vectors, vectorSize, node)) {
                        oddWins.set(node);
                    } else {
                        evenWins.set(node);
                    }
                }
                getLog().send(MessagesCoalition.COALITION_JURDZINSKI_DONE, watch.getTimeSeconds());
                return new QualitativeResult(evenWins, oddWins, strategies);
    }

    private int computeDecision(int node, int[] vectors, int vectorSize, int[] previousValue) {
        int numSuccessors = game.getNumSuccessors(node);
        storeVector(vectors, vectorSize, game.getSuccessorNode(node, 0), previousValue);
        Player player = this.player.getEnum(node);
        int dir = player == Player.ONE ? 1 : -1;
        int decision = 0;
        for (int succNr = 1; succNr < numSuccessors; succNr++) {
            int succNode = game.getSuccessorNode(node, succNr);
            if (compareVectors(vectors, vectorSize, succNode, node) * dir < 0) {
                storeVector(vectors, vectorSize, succNode, previousValue);
                setVector(vectors, vectorSize, node, succNode);
                decision = succNr;
            }
        }
        return decision;
    }

    private int computeMaxPriority() {
        int numNodes = game.getNumNodes();
        int maxPriority = 0;
        NodeProperty stochasticLabels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < numNodes; node++) {
            AutomatonParityLabel label = stochasticLabels.getObject(node);
            int priority = label.getPriority();
            maxPriority = Math.max(maxPriority, priority);
        }
        return maxPriority;
    }

    /**
     * <p>
     * &quot;Eine Implementierung nimmt deshalb nur Zähler für verlierende
     * Prioritäten. (Das ist es, was die Komplexität drückt.) Diese Zähler sind
     * endlich, ein Zähler für Priorität p is auf die Anzahl von Knoten der
     * Priorität p beschränkt. Sie nimmt diese Zähler für jeden Knoten im Spiel.
     * Diese Zähler werden als Vektoren betrachtet.&quot;
     * </p>
     * <p>
     * Returns the counter bounds for uneven priorities. Given an uneven
     * priority p, its bound is stored in result entry p / 2 (integer division).
     * The bound is inclusive.
     * </p>
     * 
     * @return bounds for uneven priorities
     */
    private int[] computeCounterBounds() {
        int[] bounds = new int[(maxPriority + 1) / 2];
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        int numNodes = game.getNumNodes();
        for (int node = 0; node < numNodes; node++) {
            AutomatonParityLabel label = labels.getObject(node);
            int priority = label.getPriority();
            if (priority % 2 == 0) {
                continue;
            }
            bounds[priority / 2]++;
        }
        return bounds;
    }

    private void liftSuccessorAll(int[] vectors, int vectorSize, int[] counterBounds, NodeProperty labels, int[] previousValue) {
        boolean changed = true;
        int numNodes = game.getNumNodes();
        StopWatch iterationWatch = new StopWatch(true);
        long liftsPerformed = 0L;
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_LIFTING_START);
        long numChanged = 0;
        long completeRounds = 0;
        while (changed) {
            completeRounds++;
            changed = false;
            for (int node = 0; node < numNodes; node++) {
                liftsPerformed++;
                boolean nodeChanged = liftNode(vectors, vectorSize, counterBounds, labels, node, previousValue); 
                changed = changed || nodeChanged;
                if (nodeChanged) {
                    numChanged++;
                }
            }
        }
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_LIFTING_DONE,
                iterationWatch.getTimeSeconds(),
                completeRounds,
                liftsPerformed,
                numChanged);
    }

    private void liftSuccessorChanged(int[] vectors, int vectorSize, int[] counterBounds, NodeProperty labels, int[] previousValue) {		
        StopWatch predecessorsWatch = new StopWatch(true);
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_COMPUTE_PREDECESSORS_START);
        game.computePredecessors();
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_COMPUTE_PREDECESSORS_DONE, predecessorsWatch.getTimeSeconds());
        StopWatch iterationWatch = new StopWatch(true);
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_LIFTING_START);
        int qualitativeGameNumNodes = game.getNumNodes();
        BitSet candidatesMarkers = UtilBitSet.newBitSetBounded(qualitativeGameNumNodes);
        IntDeque candidates = new IntDeque();
        int numCandidates = 0;
        for (int node = 0; node < qualitativeGameNumNodes; node++) {
            candidates.addLast(node);
            candidatesMarkers.set(node);
            numCandidates++;
        }
        long liftsPerformed = 0L;
        long completeRounds = 0L;
        long numChanged = 0L;
        OptionsCoalition.JurdzinskyLiftOrder order =
                Options.get().getEnum(OptionsCoalition.COALITION_JURDZINSKY_LIFT_ORDER);
        boolean fifo = order == JurdzinskyLiftOrder.FIFO;
        while (!candidates.isEmpty()) {
            completeRounds++;
            int node = fifo ? candidates.removeFirst() : candidates.removeLast();
            candidatesMarkers.set(node, false);
            numCandidates--;
            boolean nodeChanged = liftNode(vectors, vectorSize, counterBounds, labels, node, previousValue); 
            liftsPerformed++;
            if (nodeChanged) {
                numChanged++;
                int numPredecessors = game.getProperties().getNumPredecessors(node);
                for (int predNr = 0; predNr < numPredecessors; predNr++) {
                    int predNode = game.getProperties().getPredecessorNode(node, predNr);
                    if (!candidatesMarkers.get(predNode)) {
                        numCandidates++;
                        candidates.addLast(predNode);
                        candidatesMarkers.set(predNode);
                    }
                }
            }
            assert numCandidates == candidatesMarkers.cardinality();
        }
        getLog().send(MessagesCoalition.COALITION_JURDZINSKI_LIFTING_DONE,
                iterationWatch.getTimeSeconds(),
                completeRounds,
                liftsPerformed,
                numChanged);
    }

    /**
     * Lift node.
     * Returns {@code true} if changed.
     * 
     * @param vectors
     * @param vectorSize
     * @param counterBounds
     * @param labels
     * @param node
     * @param previousValue
     * @return
     */
    private boolean liftNode(int[] vectors, int vectorSize, int[] counterBounds, NodeProperty labels, int node, int[] previousValue) {
        AutomatonParityLabel label = labels.getObject(node);
        int priority = label.getPriority();
        int numSuccessors = game.getNumSuccessors(node);
        storeVector(vectors, vectorSize, node, previousValue);
        setVector(vectors, vectorSize, node, game.getSuccessorNode(node, 0));
        /* Für einen Knoten v, der Even gehör:
         * nimm den minimalen Wert der Vektoren der nachfolgenden Knoten
         * (Even will ja den Wert klein halten)
         * Für einen Knoten der Odd gehört:
         * nimm den maximalen Wert der Vektoren der nachfolgenden Knoten
         * (Odd will den Wert groß kriegen -- und am Ende das maximale
         * Element erreichen)
         * der Rest ist gleich: [...]
         */
        Player player = this.player.getEnum(node);
        int dir = player == Player.ONE ? 1 : -1;
        for (int succNr = 1; succNr < numSuccessors; succNr++) {
            int succNode = game.getSuccessorNode(node, succNr);
            if (compareVectors(vectors, vectorSize, succNode, node) * dir < 0) {
                setVector(vectors, vectorSize, node, succNode);
            }
        }
        if (isVectorMaxElem(vectors, vectorSize, node)) {
            return !equalsVector(vectors, vectorSize, node, previousValue);
        }

        /* 2. "wenn der knoten Priorität p hat, setze alle Zähler von v die zu
         * einer Priorität < p gehören auf 0"
         * We are using min parity, thus reset for prio > p
         */
        int entryNumber = (priority / 2) + (priority % 2 == 0 ? -1 : 0);
        for (int preEntryNumber = entryNumber + 1; preEntryNumber < vectorSize; preEntryNumber++) {
            setVectorEntry(vectors, vectorSize, node, preEntryNumber, 0);
        }
        if (priority % 2 == 0) {
            return !equalsVector(vectors, vectorSize, node, previousValue);
        }
        /* 3. wenn p ungerade ist, ... */

        /* erhöhe den Wert the Zählers für p
         * Schleife: wenn dieser Zähler überläuft, setze ihn auf 0, und erhöhe
         * den nächste Zähler um 1 (als nächstes für p+2, dann p+4, etc.)
         * Wenn der höchste Zähler überläuft, nimm statt dessen ein maximales
         * Element, das bedeutet dass Odd gewinnt
         * As we are using min parity games, we have to reverse the order of the
         * loop
         */
        incVectorEntry(vectors, vectorSize, node, entryNumber);
        while (entryNumber >= 0 &&
                getVectorEntry(vectors, vectorSize, node, entryNumber) > counterBounds[entryNumber]) {
            setVectorEntry(vectors, vectorSize, node, entryNumber, 0);
            entryNumber--;
            if (entryNumber >= 0) {
                incVectorEntry(vectors, vectorSize, node, entryNumber);
            }
        }
        if (entryNumber < 0) {
            setVectorMaxElem(vectors, vectorSize, node);
        }
        return !equalsVector(vectors, vectorSize, node, previousValue);
    }

    /**
     * <p>
     * &quot;Der Wert eines Knoten ist der Wert der Zähler mit einer
     * alphabetischen Ordnung, die den Zähler für höhere Prioritäten ein höheres
     * gewicht beimisst.&quot;
     * </p>
     * <p>
     * Compares two priority counter vectors.
     * Both vectors are stored in the integer array denoted by the
     * {@code vectors} parameter. Entries {@code node1 * size} inclusive to
     * {@code node1 * size + size} exclusive store the values of the first
     * vector. Entries {@code node2 * size} inclusive to {@code node2 * size +
     * size} exclusive store the values of the second vector. Vectors are
     * compared using an alphabetic order. The first entries correspond to lower
     * priorities and are thus weighted more, because we are considering min
     * parity games.
     * </p>
     * <p>
     * Note that, in contrast to the description in German above, because we are
     * using min parity games, indeed counters for <emph>lower</emph> priorities
     * have a higher weight.
     * </p>
     * 
     * @param vectors array storing values of 
     * @param size size of a vector
     * @param node1 position of vector 1
     * @param node2 position of vector 2
     * @return first vector is smaller (-1), equal to (0), or larger (1) second
     */
    private static int compareVectors(int[] vectors, int size, int node1, int node2) {
        assert vectors != null;
        assert size >= 0;
        assert vectors.length % size == 0;
        assert size * node1 + size <= vectors.length;
        for (int entryNumber = 0; entryNumber < size; entryNumber++) {
            int entry1 = vectors[size * node1 + entryNumber];
            int entry2 = vectors[size * node2 + entryNumber];
            if (entry1 < entry2) {
                return -1;
            } else if (entry1 > entry2) {
                return 1;
            }
        }
        return 0;
    }

    private static void setVector(int[] vectors, int size, int to, int from) {
        System.arraycopy(vectors, size * from, vectors, size * to, size);
    }

    private static void setVectorEntry(int[] vectors, int size, int state, int entryNr, int value) {
        vectors[size * state + entryNr] = value;
    }

    private static int getVectorEntry(int[] vectors, int size, int state, int entryNr) {
        return vectors[size * state + entryNr];
    }

    private static void incVectorEntry(int[] vectors, int size, int state, int entryNr) {
        vectors[size * state + entryNr]++;
    }

    private static void setVectorMaxElem(int[] vectors, int size, int to) {
        for (int i = 0; i < size; i++) {
            vectors[size * to + i] = Integer.MAX_VALUE;
        }
    }

    private static boolean isVectorMaxElem(int[] vectors, int size, int node) {
        assert vectors != null;
        assert size >= 0;
        assert node >= 0;
        if (size == 0) {
            return false;
        }
        assert size * node + 0 < vectors.length : (size * node + 0) + " " + vectors.length;
        return vectors[size * node] == Integer.MAX_VALUE;
    }

    private static void storeVector(int[] vectors, int size, int node, int[] previousValue) {
        System.arraycopy(vectors, size * node, previousValue, 0, size);
    }

    private static boolean equalsVector(int[] vectors, int size, int node, int[] previousValue) {
        for (int entryNumber = 0; entryNumber < size; entryNumber++) {
            if (previousValue[entryNumber] != vectors[size * node + entryNumber]) {
                return false;
            }
        }		
        return true;
    }

    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
