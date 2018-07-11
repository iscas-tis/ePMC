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

package epmc.lumpingdd;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.dd.Walker;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.dd.GraphDD;
import epmc.graphsolver.lumping.LumperDD;
import epmc.modelchecker.ModelChecker;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorOr;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;

public class LumperDDSignature implements LumperDD {
    private final static String BLOCK_INDEX = "%block_index";
    private GraphDD original;
    private List<Expression> requireValidFor;
    private DD partitionADD;
    private VariableDD blockIndexVar;
    private ContextDD contextDD;
    private Signature signature;
    private ModelChecker modelChecker;
    private String identifier;

    public LumperDDSignature(Signature s, String id) {
        this.requireValidFor = new ArrayList<>();
        this.signature = s;
        this.identifier = id;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    /**
     * Translates an expression over the original state space
     * to an expression over the quotient state space. This
     * can be used when one want to check a property on the 
     * quotient system.
     * @param expression The expression over the original state space
     * @return A translated expression over the quotient state space
     */
    @Override
    public Expression getQuotientExpression(Expression expression) {
        ExpressionToDD etdd = original.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        DD blockPresCube = blockIndexVar.newCube(0);
        Map<Expression, Expression> replacements = new HashMap<>();
        for (Expression label : computeAllStateLabels(expression)) {
            DD labelBlocks = originalToQuotient(etdd.translate(label));
            TIntSet satBlock = collectSat(labelBlocks.walker(), blockPresCube.walker(), new HashMap<Tuple, TIntSet>());
            int[] satBlockArray = satBlock.toArray();
            Arrays.parallelSort(satBlockArray);
            List<int[]> satBlockRanges = splitRange(satBlockArray);
            labelBlocks.dispose();
            Expression blockIndexId = newIdentifier(BLOCK_INDEX);
            List<Expression> treeList = new ArrayList<>();
            for(int[] range: satBlockRanges) {
                if(range[1] - range[0] >= 1) {
                    addToBalancedTree(treeList, and(ge(blockIndexId, newLiteral(range[0])),
                            le(blockIndexId, newLiteral(range[1]))));
                } else {
                    addToBalancedTree(treeList, eq(blockIndexId, newLiteral(range[0])));
                }
            }
            replacements.put(label, makeFinalTree(treeList, getFalse()));
        }
        blockPresCube.dispose();
        Expression result = UtilExpressionStandard.replace(expression, replacements);
        return result;
    }

    private List<Expression> addToBalancedTree(List<Expression> levels, Expression expr) {
        for(int i = 0; ; i++) {
            if(levels.size() == i) {
                levels.add(expr);
                break;
            } else if(levels.get(i) == null) {
                levels.set(i, expr);
                break;
            } else {
                expr = or(levels.get(i), expr);
                levels.set(i, null);
            }
        }
        return levels;
    }

    private Expression makeFinalTree(List<Expression> levels, Expression falseExpr) {
        Expression result = falseExpr;
        for(int i = 0; i < levels.size(); i++) {
            if (levels.get(i) != null) {
                result = or(result, levels.get(i));
            }
        }
        return result;
    }

    /**
     * Returns a list of ranges of integers that
     * are present in a. a should be a sorted list.
     * @param a A sorted array of integers
     * @return A list with ranges defining the integers
     * present in a.
     */
    private List<int[]> splitRange(int[] a) {
        List<int[]> result = new ArrayList<>();
        if(a.length == 0) {
            return result;
        }
        int leftbound = a[0];
        for(int i = 1; i < a.length; i++) {
            if(a[i] > a[i-1] + 1) {
                int rightbound = a[i-1];
                result.add(new int[]{leftbound,rightbound});
                leftbound = a[i];
            }
        }
        result.add(new int[]{leftbound, a[a.length - 1]});
        return result;
    }

    private ExpressionToDD getQuotientExpressionToDD() {
        Map<Expression, VariableDD> variables = new HashMap<>();
        variables.put(newIdentifier(BLOCK_INDEX), blockIndexVar);
        return new ExpressionToDD(variables);
    }

    private class Tuple {
        private long fst;
        private long snd;

        public Tuple(long first, long second) {
            fst = first; snd = second;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = (int)(fst^(fst>>>32)) + (hash << 6) + (hash << 16) - hash;
            hash = (int)(snd^(snd>>>32)) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof Tuple) && ((Tuple) other).fst == fst && ((Tuple) other).snd == snd;
        }
    }

    /**
     * Computes the set of integers that represent the satisfying assignments
     * of w. The variable that is stored highest in the DD will become the
     * least significant bit in the integer representation.
     * @param w Walker of the DD to collect satisfying assignments for.
     * @param cube A conjunction of variables defining which variables to use in the assignment
     * @param cache A computation cache, typically an empty map
     * @return The set of satisfying assignments, represented by integers
     */
    private TIntSet collectSat(Walker w, Walker cube, Map<Tuple, TIntSet> cache) {
        Tuple t = new Tuple(w.uniqueId(), cube.uniqueId());
        if(cache.containsKey(t)) {
            return cache.get(t);
        }

        TIntSet result = new TIntHashSet();
        if(w.isLeaf() && cube.isLeaf()) {
            if(w.isTrue()) {
                result.add(0);
            }
        } else if(cube.isLeaf()) {
            assert false : "The cube should have at least all the variables in the support of w";
        } else if(w.isLeaf()) {
            int i = 0;
            while(!cube.isLeaf()) {
                cube.high();
                i++;
            }
            TIntIterator it = collectSat(w, cube, cache).iterator();
            while(it.hasNext()) {
                int recursiveSat = it.next() << i;
                for(int x = 0; x < (1 << i); x++) {
                    result.add(recursiveSat + x);
                }
            }
            for(int j = 0; j < i; j++) {
                cube.back();
            }
        } else if(w.variable() == cube.variable()) {
            w.low(); cube.high();
            TIntSet low = collectSat(w, cube, cache);
            w.back();
            w.high();
            TIntSet high = collectSat(w, cube, cache);
            w.back(); cube.back();
            TIntIterator it = low.iterator();
            while(it.hasNext()) {
                result.add(it.next() << 1 | 0);
            }
            it = high.iterator();
            while(it.hasNext()) {
                result.add(it.next() << 1 | 1);
            }
        } else {
            int i = 0;
            while(cube.variable() < w.variable()) {
                // Walk down the cube
                i++;
                cube.high();
            }
            TIntIterator it = collectSat(w, cube, cache).iterator();
            while(it.hasNext()) {
                int recursiveSat = it.next() << i;
                for(int x = 0; x < (1 << i); x++) {
                    result.add(recursiveSat + x);
                }
            }
            for(int j = 0; j < i; j++) {
                // Walk the same amount of steps up
                cube.back();
            }
        }
        cache.put(t, result);
        return result;
    }

    public static DD satOneWith(DD dd, DD cube) {
        DD result = pickOne(dd, cube);
        dd.dispose();
        cube.dispose();
        return result;
    }

    /**
     * A function that can be used to pick one state for every
     * block in a partition. This function is not tested well for
     * cases walking over the dd and the cube leads to a position
     * where the cube is not a leaf and the dd variable is smaller
     * than the cube variable.
     * @param dd The dd to pick assignments in
     * @param cube The cube of variables from which to pick one assignment
     */
    public static DD pickOne(DD dd, DD cube) {
        assert cube.assertCube();
        assert TypeBoolean.is(dd.getType()) || TypeReal.is(dd.getType()) || TypeInteger.is(dd.getType());
        TLongObjectMap<DD> cache = new TLongObjectHashMap<>();
        DD falseTerminal = dd.getContext().newConstant(falseValueForType(dd.getType()));
        DD result = pickOne(dd.walker(), cube.walker(), falseTerminal, dd.getContext(), cache).clone();
        disposeCachedDDs(cache);
        if(falseTerminal.alive()) {
            falseTerminal.dispose();
        }
        return result;
    }

    private static DD pickOne(Walker w, Walker cube, DD falseTerminal, ContextDD contextDD, TLongObjectMap<DD> cache) {
        if(cache.containsKey(w.uniqueId())) {
            return cache.get(w.uniqueId());
        }
        DD result;
        if(w.isLeaf() && cube.isLeaf()) {
            result = contextDD.newConstant(w.value());
        } else if(w.isLeaf()) {
            cube.high();
            result = pickOne(w, cube, falseTerminal, contextDD, cache);
            cube.back();
            result = contextDD.variable(cube.variable()).ite(falseTerminal, result);
        } else if(cube.isLeaf()) {
            // The cube is finished, just reconstruct the
            // rest of the tree
            w.low();
            DD low = pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            w.high();
            DD high = pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            result = contextDD.variable(w.variable()).ite(high, low);
        } else if(w.variable() == cube.variable()) {
            // We should pick one
            // assignment. If a lower node is in the cache
            // an assignment reaching that node was found before.
            w.low(); cube.high();
            DD low = cache.containsKey(w.uniqueId()) ? falseTerminal : pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            w.high();
            DD high = cache.containsKey(w.uniqueId()) ? falseTerminal : pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            cube.back();
            result = contextDD.variable(w.variable()).ite(high, low);
        } else if(w.variable() < cube.variable()) {
            // We are not at a node for which to one
            // assignment, so just combine the recursive
            // results
            w.low();
            DD low = pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            w.high();
            DD high = pickOne(w, cube, falseTerminal, contextDD, cache);
            w.back();
            result = contextDD.variable(w.variable()).ite(high, low);
        } else {
            // A variable in the cube was not found,
            // so we choose false as an assignment
            cube.high();
            result = pickOne(w, cube, falseTerminal, contextDD, cache);
            cube.back();
            result = contextDD.variable(cube.variable()).ite(falseTerminal, result);
        }
        cache.put(w.uniqueId(), result);
        return result;
    }

    private static Value falseValueForType(Type t) {
        assert TypeBoolean.is(t) || TypeReal.is(t) || TypeInteger.is(t);
        Value falseValue;
        if (TypeBoolean.is(t)) {
            TypeBoolean tBoolean = TypeBoolean.as(t);
            falseValue = UtilValue.newValue(tBoolean,false);
        } else if (TypeReal.is(t) || TypeInteger.is(t)) {
            TypeReal tReal = TypeReal.as(t);
            falseValue = UtilValue.newValue(tReal, 0);
        } else {
            falseValue = null;
            assert false;
        }
        return falseValue;
    }

    @Override
    public void setOriginal(GraphDD graph) {
        assert graph != null;
        this.original = graph;
        contextDD = original.getContextDD();
        signature.setOriginal(original);
    }

    @Override
    public void requireValidFor(Expression property) {
        assert property != null;
        this.requireValidFor.add(property);
    }

    @Override
    public boolean canLump() {
        return signature.canLump(requireValidFor);
    }

    @Override
    public void lump() {
        long time = System.nanoTime();

        partitionADD = makeInitialPartition();
        System.out.println("Computing initial partition: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time) + " milliseconds");
        time = System.nanoTime();
        signature.setBlockIndexVar(blockIndexVar);
        System.out.println("Initializing signature: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time) + " milliseconds");
        DD prevPartitionsDD = partitionADD.clone();
        DD signaturesADD;
        int i = 0;
        do {
            prevPartitionsDD.dispose();
            prevPartitionsDD = partitionADD.clone();
            time = System.nanoTime();
            signaturesADD = signature.computeSignatures(partitionADD);
            long sig = System.nanoTime() - time;

            partitionADD.dispose();
            partitionADD = refine(signaturesADD, blockIndexVar).toMTWith();
            long part = System.nanoTime() - time - sig;
            System.out.printf("sig: %d; ref: %d;\n", TimeUnit.NANOSECONDS.toMillis(sig),
                    TimeUnit.NANOSECONDS.toMillis(part));

            signaturesADD.dispose();
            i++;
        } while(!prevPartitionsDD.equals(partitionADD));
        prevPartitionsDD.dispose();

        int stateSize = partitionADD.eq(contextDD.newConstant(1))
                .abstractExistWith(original.getPresCube().clone())
                .countSatWith(blockIndexVar.newCube(0)).intValue();
        partitionADD = reduceBlockIndexRange(stateSize - 1, partitionADD);
        System.out.println("Loops: " + i);
    }

    /**
     * Make the initial partition by putting states with
     * the same labels in the same block.
     * @return A DD defining the initial partition
     */
    private DD makeInitialPartition() {

        List<DD> labels = new ArrayList<>();
        List<DD> reward = new ArrayList<>();
        Collection<Object> validForProps;
        if(requireValidFor.isEmpty()) {
            // If no properties were set, just use all
            // the node properties
            validForProps = original.getNodeProperties();
        } else {
            // Otherwise, compute all the node properties
            // we have to consider
            validForProps = new HashSet<>();
            for (Expression expr: requireValidFor) {
                validForProps.addAll(collectRewards(expr));
                validForProps.addAll(computeAllStateLabels(expr));
            }
        }

        DD zeroDD = contextDD.newConstant(0);
        for(Object prop: validForProps) {
            if(prop instanceof RewardSpecification) {
                reward.add(original.getNodeProperty(prop));
                assert original.getEdgeProperty(prop).equals(zeroDD) : "Edge rewards are not supported when lumping";
            } else if(prop instanceof DD) {
                labels.add(original.getNodeProperty(prop));
            } else if(prop instanceof Expression) {
                labels.add(original.getNodeProperty(prop));
            }
        }

        DD stateSpace = original.getNodeSpace().and(original.getNodeProperty(CommonProperties.STATE));
        // Count all the states. Don't count nondet states, because they will not be
        // put in a partition.
        int stateSpaceSize = stateSpace.countSat(original.getPresCube()).intValueExact();
        blockIndexVar = contextDD.newInteger(BLOCK_INDEX, 2,
                0, Math.max(stateSpaceSize, (int) Math.pow(2, labels.size()) - 1));

        List<DD> blockIndexVars = blockIndexVar.getDDVariables(0);
        DD result = stateSpace.clone();
        int numPartitions = 0;
        int oldNumPartitions = 0;
        int blockCount = 0;
        for(DD label: labels) {
            // Fix one block index variable for each label
            DD statesWithLabel    = stateSpace.and(label)
                    .andWith(blockIndexVars.get(blockCount).clone());
            DD statesWithoutLabel = stateSpace.andNot(label)
                    .andNotWith(blockIndexVars.get(blockCount).clone());

            DD oldResult = result.clone();
            result = result.andWith(statesWithLabel.orWith(statesWithoutLabel));

            // Compute the number of partitions there are now
            oldNumPartitions = numPartitions;
            numPartitions = numPartitions(result.walker(), new TLongHashSet());
            if(oldNumPartitions == numPartitions) {
                // The amount of partitions didn't increase, so
                // the label under consideration does not matter.
                // We will use the old partition, which is a smaller DD
                result.dispose();
                result = oldResult;
            } else {
                // The partition was refined, so just continue
                oldResult.dispose();
                blockCount++;
            }
        }
        stateSpace.dispose();

        // Nice trick: use the refine algorithm to reduce the
        // size of the BDD and to bring all the used block 
        // numbers below the stateSpaceSize
        result = refine(result, blockIndexVar);
        // The upper limit of blockIndexVar should be reduced
        // to stateSpaceSize. The other vars are not needed anymore.
        result = reduceBlockIndexRange(stateSpaceSize, result);

        // Further refine the initial partitions according
        // to the state rewards.
        DD trueDD = contextDD.newConstant(true);
        VariableDD rewardIndex = contextDD.newInteger("%rewardIndex", 1, 0, stateSpaceSize - 1);
        for(DD stateReward: reward) {
            Set<Value> values = new HashSet<>();
            stateReward.collectValues(values, trueDD);
            values.remove(null);
            DD valueIndex = contextDD.newConstant(0);
            int currValue = 0;
            for(Value v: values) {
                DD thisValueDD = contextDD.newConstant(v);
                valueIndex = valueIndex.addWith(rewardIndex.newIntValue(0, currValue).iteWith(thisValueDD, zeroDD.clone()));
                currValue++;
            }
            result = result.andWith(stateReward.clone().eqWith(valueIndex));
            DD oldResult = result;
            result = refine(result, blockIndexVar);
            oldResult.dispose();
        }
        trueDD.dispose();
        zeroDD.dispose();
        rewardIndex.close();
        return result.toMTWith();
    }

    /**
     * Reduce the range of blockIndexVar to save DD variables
     * @param newUpper The new upper bound
     * @param partition The partitionDD to be translated
     * @return The new partitionDD
     */
    private DD reduceBlockIndexRange(int newUpper, DD partition) {
        DD result = partition;
        if(blockIndexVar.getUpper() > newUpper) {
            VariableDD oldBlockIndexVar = blockIndexVar;
            blockIndexVar = contextDD.newInteger(BLOCK_INDEX, 2,
                    0, newUpper);
            List<DD> oldVars = oldBlockIndexVar.getDDVariables(0);
            List<DD> newVars = blockIndexVar.getDDVariables(0);
            if (TypeBoolean.is(result.getType())) {
                result = result.abstractExistWith(contextDD.listToCube(oldVars.subList(newVars.size(), oldVars.size())));
            } else {
                result = result.abstractSumWith(contextDD.listToCube(oldVars.subList(newVars.size(), oldVars.size())));
            }
            result = result.permuteWith(contextDD.newPermutationListDD(oldVars.subList(0, newVars.size()), newVars));
            oldBlockIndexVar.close();
        }
        return result;
    }

    static List<RewardSpecification> collectRewards(Expression expr) {
        List<RewardSpecification> result = new LinkedList<>();
        Queue<Expression> children = new LinkedList<>();
        children.offer(expr);
        Expression child;
        while ((child = children.poll()) != null) {
            if (ExpressionReward.is(child)) {
                result.add(ExpressionReward.as(child).getReward());
            } else {
                children.addAll(child.getChildren());
            }
        }
        return result;
    }

    private int numPartitions(Walker w, TLongSet visitedCache) {
        if(visitedCache.contains(w.uniqueId())) {
            return 0;
        }
        visitedCache.add(w.uniqueId());
        if(w.isLeaf()) {
            return 0;
        } else if(w.variable() >= blockIndexVar.getDDVariables(0).get(0).variable()) {
            return 1;
        } else {
            w.low();
            int low = numPartitions(w, visitedCache);
            w.back();
            w.high();
            int high = numPartitions(w, visitedCache);
            w.back();
            return low + high;
        }
    }

    /**
     * Refine the partition based on the newly computed
     * signatures. Each signature is replaced by a block
     * index.
     */
    private DD refine(DD signatures, VariableDD blockIndex) {
        Counter blockCounter = new Counter();
        TLongObjectMap<DD> cache = new TLongObjectHashMap<>();
        DD signaturesReplaced = replaceSignatures(signatures.walker(), blockIndex, blockCounter, cache).clone();
        disposeCachedDDs(cache);
        if(blockCounter.value == 0) {
            //Not a single block was created, maybe due to all the same signatures
            //We do it ourselves
            DD stateSpace = original.getNodeSpace().and(original.getNodeProperty(CommonProperties.STATE));
            return stateSpace.andWith(blockIndex.newIntValue(0, 0));
        } else {
            return signaturesReplaced;
        }
    }

    private class Counter {
        int value;
    }

    /**
     * Replace signatures by block index numbers
     * @param w A walker of the DD defining the signatures
     * @param blockIndex The variable to use to specify the block index
     * @param blockCount A counter that counts the current block number
     * @param computedTable A cache to store DD nodes already seen
     * @return A DD defining the partitions based on the signatures
     */
    private DD replaceSignatures(Walker w, VariableDD blockIndex,
            Counter blockCount, TLongObjectMap<DD> computedTable) {
        if(computedTable.containsKey(w.uniqueId())) {
            return computedTable.get(w.uniqueId());
        }

        DD result;
        if(w.isLeaf()) {
            result = contextDD.newConstant(false);
        } else if(w.variable() >= blockIndex.getDDVariables(0).get(0).variable()) {
            result = blockIndex.newIntValue(0, blockCount.value);
            blockCount.value++;
        } else {
            w.low();
            DD low = replaceSignatures(w, blockIndex, blockCount, computedTable);
            w.back();
            w.high();
            DD high = replaceSignatures(w, blockIndex, blockCount, computedTable);
            w.back();
            DD thisNode = contextDD.variable(w.variable());
            result = thisNode.ite(high, low);
        }
        computedTable.put(w.uniqueId(), result);
        return result;
    }

    /**
     * Dispose all the DDs in the given cache.
     */
    public static void disposeCachedDDs(TLongObjectMap<DD> cache) {
        for(DD dd : cache.valueCollection()) {
            dd.dispose();
        }
    }

    /**
     * Open dd with the program xdot.
     * Requires that xdot is in your path and
     * that a /tmp directory is present.
     * @param dd The dd to open
     */
    public static void openDDXdot(DD dd) {
        openGraphXdot(dd.toString());
    }

    private static void openGraphXdot(String graph) {
        File output = new File("/tmp/epmc" + System.currentTimeMillis() + ".dot");
        try {
            FileOutputStream out = new FileOutputStream(output);
            out.write(graph.getBytes());
            out.close();
            Runtime.getRuntime().exec("xdot " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean originalIsNonDetStateDistribution() {
        return false;
        //    	return ((Semantics) original.getGraphPropertyObject(CommonProperties.SEMANTICS)).isNonDet()
        //  			&& enc == OptionsTypesEPMC.MDPEncoding.STATE_DISTRIBUTION;
    }

    /**
     * Build the DD that defines the transition weights of
     * the quotient graph
     */
    private DD getQuotientTransWeights() {
        DD presCube = original.getPresCube().and(blockIndexVar.newCube(0));
        DD nextCube = original.getNextCube().and(blockIndexVar.newCube(1));
        Permutation p = contextDD.newPermutationCube(presCube, nextCube);
        presCube.dispose();
        nextCube.dispose();
        if(originalIsNonDetStateDistribution() ) {
            DD states = original.getNodeProperty(CommonProperties.STATE);
            DD nondetVarCube = states.supportDD();
            DD stateVariablesCube = original.getPresCube().abstractExist(nondetVarCube);
            DD statePartitions = partitionADD.clone().multiplyWith(states.toMT()).abstractSumWith(nondetVarCube);
            DD statePartitionsNext = statePartitions.permute(p);
            DD statePartitionsSatOne = LumperDDSignature.pickOne(statePartitions, stateVariablesCube);
            statePartitions.dispose();

            DD nodeSpace = original.getNodeSpace().toMT()
                    .multiplyWith(original.getEdgeProperty(CommonProperties.WEIGHT).clone())
                    .abstractSumWith(original.getPresCube().and(original.getActionCube()))
                    .permuteWith(original.getSwapPresNext())
                    .neWith(contextDD.newConstant(newValueWeightTransition("0")))
                    .orWith(original.getNodeSpace().and(states));
            DD nondetTrans = original.getEdgeProperty(CommonProperties.WEIGHT).clone()
                    .multiplyWith(nodeSpace.toMT(), statePartitionsSatOne, statePartitionsNext)
                    .abstractSumWith(stateVariablesCube.andWith(stateVariablesCube.permute(original.getSwapPresNext())));

            return nondetTrans;
        } else {
            DD partitionsNext = partitionADD.permute(p);
            DD oneStatePartition = LumperDDSignature.pickOne(partitionADD, original.getPresCube());
            DD quotientTransitions = original.getEdgeProperty(CommonProperties.WEIGHT).clone()
                    .multiplyWith(oneStatePartition, partitionsNext);
            quotientTransitions = quotientTransitions.abstractSumWith(original.getPresCube().and(original.getNextCube()));
            return quotientTransitions;
        }
    }

    private List<DD> cubeToList(DD cube) {
        List<DD> result = new ArrayList<>();
        Walker w = cube.walker();
        while(!w.isLeaf()) {
            result.add(contextDD.variable(w.variable()));
            w.high();
        }
        return result;
    }

    @Override
    public GraphDD getQuotient() {
        DD quotientTransWeightsDD = getQuotientTransWeights();
        DD partitionsBDD = partitionADD.clone().eqWith(contextDD.newConstant(1));

        DD states = original.getNodeProperty(CommonProperties.STATE);
        // Compute the node space including all the nondet states
        DD nodeSpace = original.getNodeSpace().toMT()
                .multiplyWith(original.getEdgeProperty(CommonProperties.WEIGHT).clone())
                .abstractSumWith(original.getPresCube().and(original.getActionCube()))
                .permuteWith(original.getSwapPresNext())
                .neWith(contextDD.newConstant(newValueWeightTransition("0")))
                .orWith(original.getNodeSpace().and(states));
        // presCube with only the nondet vars
        DD nondetVariablesCube = states.supportDD();
        DD nondetVariablesCubeNext = nondetVariablesCube.permute(original.getSwapPresNext());
        // presCube without the nondet vars
        DD stateVariablesCube = original.getPresCube().abstractExist(nondetVariablesCube);
        // Nondet states get the block number associated to their state variables
        // Only abstract over the state variables, because we want to keep the nondet variables.
        DD quotientNodeSpace = partitionsBDD.abstractExist(nondetVariablesCube)
                .abstractAndExistWith(nodeSpace.clone(),stateVariablesCube.clone());
        DD quotientStates = original.getNodeProperty(CommonProperties.STATE)
                .abstractAndExist(partitionsBDD, stateVariablesCube);

        // Make a copy of the block index vars and add the nondet vars
        List<DD> presVars = new ArrayList<>(blockIndexVar.getDDVariables(0));
        List<DD> nextVars = new ArrayList<>(blockIndexVar.getDDVariables(1));
        presVars.addAll(cubeToList(nondetVariablesCube));
        nextVars.addAll(cubeToList(nondetVariablesCubeNext));
        nondetVariablesCubeNext.dispose();

        TypeEnum playerType = TypeEnum.get(Player.class);
        DD playerStochastic = contextDD.newConstant(playerType.newValue(Player.STOCHASTIC));
        DD playerOne = contextDD.newConstant(playerType.newValue(Player.ONE));
        DD quotientPlayer;
        if(originalIsNonDetStateDistribution()) {
            quotientPlayer = quotientStates.ite(playerOne, playerStochastic);
        } else {
            quotientPlayer = playerStochastic.clone();
        }
        playerStochastic.dispose();
        playerOne.dispose();

        GraphDDGeneric.Builder result = new GraphDDGeneric.Builder();
        result.registerGraphProperty(CommonProperties.SEMANTICS, getTypeObject(Semantics.class));
        result.setGraphProperty(CommonProperties.SEMANTICS, original.getGraphProperty(CommonProperties.SEMANTICS));
        if(modelChecker != null) {
            result.registerGraphProperty(CommonProperties.EXPRESSION_TO_DD,
                    getTypeObject(ExpressionToDD.class));
            result.setGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD,
                    getQuotientExpressionToDD());
        }
        result.registerNodeProperty(CommonProperties.STATE, quotientStates);
        result.registerNodeProperty(CommonProperties.PLAYER, quotientPlayer);
        result.registerEdgeProperty(CommonProperties.WEIGHT, quotientTransWeightsDD);
        result.setInitialNodes(original.getInitialNodes().abstractAndExist(partitionsBDD, stateVariablesCube));
        result.setPresVars(presVars);
        result.setNextVars(nextVars);
        result.setNextVars(cubeToList(original.getActionCube()));
        result.setNodeSpace(quotientNodeSpace);
        result.setSwapPresNext(contextDD.newPermutationListDD(presVars, nextVars));
        result.setTransitions(quotientTransWeightsDD.clone().eqWith(contextDD.newConstant(0))
                .orWith(quotientTransWeightsDD
                        .eqWith(contextDD.newConstant(newValueWeightTransition("0")))).notWith());

        partitionsBDD.dispose();
        stateVariablesCube.dispose();
        quotientPlayer.dispose();

        //        GraphBuilderDD builder = new GraphBuilderDD(result, new ArrayList<>(), false);
        //        builder.setUniformise(false);
        //        GraphExplicit lumped = builder.buildGraph();
        //        String graphString = GraphExporter.toString(lumped);
        //        openGraphXdot(GraphExporter.toString(lumped));
        //        builder.close();

        BigInteger originalStateSpace = nodeSpace.and(states).countSat(original.getPresCube());
        nodeSpace.dispose();
        DD quotientCube = nondetVariablesCube.andWith(blockIndexVar.newCube(0));
        BigInteger quotientStateSpace = quotientNodeSpace.and(quotientStates).countSatWith(quotientCube);
        System.out.println("original state space: " + originalStateSpace);
        System.out.println("quotient state space: " + quotientStateSpace);
        quotientStates.dispose();

        return result.build();
    }

    @Override
    public DD quotientToOriginal(DD quotient) {
        if (quotient.getType() == TypeBoolean.get()) {
            return quotient.abstractAndExist(partitionADD.eq(contextDD.newConstant(1)), blockIndexVar.newCube(0));
        } else {
            return quotient.multiply(partitionADD).abstractSum(blockIndexVar.newCube(0));
        }
    }

    @Override
    public DD originalToQuotient(DD original) {
        if (original.getType() == TypeBoolean.get()) {
            return original.abstractAndExist(partitionADD.eq(contextDD.newConstant(1)), this.original.getPresCube());
        } else {
            return original.multiply(partitionADD).abstractSum(this.original.getPresCube());
        }
    }

    private ExpressionIdentifier newIdentifier(String name) {
        return new ExpressionIdentifierStandard.Builder()
                .setName(name)
                .build();
    }

    private ExpressionLiteral newLiteral(Value value) {
        assert value != null;
        return new ExpressionLiteral.Builder()
                .setValue(value)
                .build();
    }

    private ExpressionLiteral newLiteral(int value) {
        ValueInteger valueInteger = UtilValue.newValue(TypeInteger.get(), value);
        return newLiteral(valueInteger);
    }

    private ValueAlgebra newValueWeightTransition(String string) {
        assert string != null;
        TypeAlgebra typeWeightTransition = TypeWeightTransition.get();
        return UtilValue.newValue(typeWeightTransition, string);
    }

    private ExpressionOperator or(Expression a, Expression b) {
        assert a != null;
        assert b != null;
        return new ExpressionOperator.Builder()
                .setOperator(OperatorOr.OR)
                .setOperands(a, b)
                .build();
    }

    private ExpressionOperator ge(Expression a, Expression b) {
        assert a != null;
        assert b != null;
        return new ExpressionOperator.Builder()
                .setOperator(OperatorGe.GE)
                .setOperands(a, b)
                .build();
    }

    private ExpressionOperator le(Expression a, Expression b) {
        assert a != null;
        assert b != null;
        return new ExpressionOperator.Builder()
                .setOperator(OperatorLe.LE)
                .setOperands(a, b)
                .build();
    }

    private ExpressionOperator eq(Expression a, Expression b) {
        assert a != null;
        assert b != null;
        return new ExpressionOperator.Builder()
                .setOperator(OperatorEq.EQ)
                .setOperands(a, b)
                .build();
    }

    private ExpressionOperator and(Expression a, Expression b) {
        assert a != null;
        assert b != null;
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAnd.AND)
                .setOperands(a, b)
                .build();
    }

    ExpressionLiteral getFalse() {
        return newLiteral(UtilValue.newValue(TypeBoolean.get(), false));
    }


    private TypeObject getTypeObject(Class<?> clazz) {
        return new TypeObject.Builder()
                .setClazz(clazz)
                .build();
    }

    private Set<Expression> computeAllStateLabels(Expression expression) {
        assert expression != null;
        if (ExpressionPropositional.is(expression)) {
            return Collections.singleton(expression);
        } else {
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression child : expression.getChildren()) {
                result.addAll(computeAllStateLabels(child));
            }
            return result;
        }
    }
}
