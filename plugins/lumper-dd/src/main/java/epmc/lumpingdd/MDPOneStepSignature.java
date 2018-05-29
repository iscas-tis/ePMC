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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.dd.Walker;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.DoubleRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

/**
 * Version of MDP one-step bisimulation signature
 * that works with GraphDDPRISM's state distribution transition representation.
 */
public class MDPOneStepSignature implements Signature {

    public static class LumperDDSignatureMDPOneStep extends LumperDDSignature {

        public final static String IDENTIFIER = "lumper-dd-signature-mdp-one-step";

        public LumperDDSignatureMDPOneStep() {
            super(new MDPOneStepSignature(), IDENTIFIER);
        }
    }

    private GraphDD original;
    private VariableDD blockIndexVar;
    private ContextDD contextDD;
    private TransitionRepresentation transRepr;
    private DD pVar;

    private DD reachStatesMax;
    private DD reachStatesMin;
    private Permutation blockPresNextPerm;
    private boolean useStateDistribution;

    @Override
    public void setOriginal(GraphDD original) {
        this.original = original;
        this.contextDD = original.getContextDD();
        //    	useStateDistribution = ((Semantics) original.getGraphPropertyObject(CommonProperties.SEMANTICS)).isNonDet()
        //  			&& enc == OptionsTypesEPMC.MDPEncoding.STATE_DISTRIBUTION;
        useStateDistribution = false;
    }

    @Override
    public boolean canLump(List<Expression> validFor) {
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        for(Expression expr: validFor) {
            if(!LumperDDSignature.collectRewards(expr).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setBlockIndexVar(VariableDD blockIndex) {
        this.blockIndexVar = blockIndex;
        this.blockPresNextPerm = contextDD.newPermutationListDD(
                blockIndexVar.getDDVariables(0), blockIndexVar.getDDVariables(1));
        pVar = contextDD.newBoolean("p", 1).getValueEncoding(0).toMT();

        VariableDD trans2Var = null;
        Permutation p = null;
        // In case we use the state distribution representation
        // of non-determinism, we will need to take two transition
        // steps to compute the maximal and minimal reachability
        // probabilities
        if (useStateDistribution) {
            trans2Var = contextDD.newBoolean("trans2", original.getNextVars().size());
            p = contextDD.newPermutationListDD(original.getNextVars(), trans2Var.getDDVariables());
        }

        this.transRepr = new DoubleRepresentation();
        this.transRepr.setOriginal(original);

        DD stateSpace = original.getNodeSpace();
        DD trans1 = transRepr.fromTransWeights();

        Set<Value> weightSet = new HashSet<>();
        DD trueDD = contextDD.newConstant(true);
        contextDD.collectValues(weightSet, trans1, trueDD);
        trueDD.dispose();
        weightSet.remove(null);
        Value[] weights = new Value[weightSet.size()];
        weights = weightSet.toArray(weights);
        Arrays.sort(weights);
        // We now have a sorted array of all weights in the transition DD
        int[] primes = generatePrimes(weights.length - 1);
        Map<Value,DD> replacementsAscending = new HashMap<>();
        Map<Value,DD> replacementsDescending = new HashMap<>();
        // Replace weight 0 by 0
        replacementsAscending.put(weights[0], contextDD.newConstant(0));
        replacementsDescending.put(weights[0], contextDD.newConstant(0));
        // Replace the other weights by ascending/descending prime numbers
        for(int i = 1; i < weights.length; i++) {
            replacementsAscending.put(weights[i], contextDD.newConstant(primes[i-1]));
            replacementsDescending.put(weights[i], contextDD.newConstant(primes[primes.length-i]));
        }
        // We make two transition relations:
        // - one where the weights are replaced by primes in ascending order
        // - one where the weights are replaced by primes in descending order
        // These two transition relations can be used to calculate the maximum
        // and minimum reachability probability respectively by only using abstractMax.
        TLongObjectMap<Tuple<DD>> cache = new TLongObjectHashMap<>();
        Tuple<DD> transTuple = replaceSymbolsTuple(
                contextDD, trans1.walker(), replacementsAscending, replacementsDescending, cache);

        if (useStateDistribution) {
            TypeAlgebra typeWeightTransition = TypeWeightTransition.get();
            ValueAlgebra zero = UtilValue.newValue(typeWeightTransition, 0);
            DD oneStepReach = stateSpace.clone().andNotWith(
                    trans1.eqWith(contextDD.newConstant(zero))).toMTWith();
            DD twoStepReachMax = oneStepReach.multiply(transTuple.fst.permute(p).permuteWith(original.getSwapPresNext()));
            DD twoStepReachMin = oneStepReach.multiply(transTuple.snd.permute(p).permuteWith(original.getSwapPresNext()));
            oneStepReach.dispose();

            // The maximum probability to reach a state
            reachStatesMax = twoStepReachMax.abstractMax(
                    original.getNextCube()).permuteWith(p);
            // The minimum probability to reach a state
            // We also use abstractMax here because the primes representing the probabilities are reversed
            reachStatesMin = twoStepReachMin.abstractMax(
                    original.getNextCube()).permuteWith(p);

            twoStepReachMax.dispose();
            twoStepReachMin.dispose();
            trans2Var.close();
        } else {
            stateSpace = stateSpace.toMT();
            // The maximum probability to reach a state
            reachStatesMax = stateSpace.multiply(transTuple.fst);
            // The minimum probability to reach a state
            reachStatesMin = stateSpace.multiply(transTuple.snd);
            stateSpace.dispose();
        }
        for(Tuple<DD> tuple : cache.valueCollection()) {
            tuple.fst.dispose();
            tuple.snd.dispose();
        }
    }

    private static class Tuple<E> {
        E fst;
        E snd;
    }
    /**
     * Replace the leafs by DDs according to the relation
     * in replacements.
     */
    private Tuple<DD> replaceSymbolsTuple(ContextDD contextDD, Walker w, Map<Value, DD> replacements1, Map<Value, DD> replacements2, TLongObjectMap<Tuple<DD>> computedCache) {
        if(computedCache.containsKey(w.uniqueId())) {
            return computedCache.get(w.uniqueId());
        }

        Tuple<DD> result = new Tuple<>();
        if(w.isLeaf()) {
            result.fst = replacements1.get(w.value());
            result.snd = replacements2.get(w.value());
        } else {
            w.low();
            Tuple<DD> low = replaceSymbolsTuple(contextDD, w, replacements1, replacements2, computedCache);
            w.back();
            w.high();
            Tuple<DD> high = replaceSymbolsTuple(contextDD, w, replacements1, replacements2, computedCache);
            w.back();
            DD thisNode = contextDD.variable(w.variable());
            result.fst = thisNode.ite(high.fst, low.fst);
            result.snd = thisNode.ite(high.snd, low.snd);
        }
        computedCache.put(w.uniqueId(), result);
        return result;
    }

    /**
     * Generate a sorted list of the first amount prime numbers
     * @param amount The amount of prime numbers to generate
     * @return A sorted list of prime numbers
     */
    private int[] generatePrimes(int amount) {
        int[] result = new int[amount];
        result[0] = 2;
        if(amount < 2) {
            return result;
        }
        result[1] = 3;
        int numFound = 2;
        int potentialPrime = 3;
        while(numFound < amount) {
            potentialPrime += 2;
            boolean isPrime = true;
            int sqrt = (int) Math.sqrt(potentialPrime);
            for(int i = 1; i < numFound; i++) {
                if(result[i] > sqrt) {
                    break;
                }
                if(potentialPrime % result[i] == 0) {
                    isPrime = false;
                    break;
                }
            }
            if(isPrime) {
                result[numFound] = potentialPrime;
                numFound++;
            }
        }
        return result;
    }

    @Override
    public TransitionRepresentation getTransitionRepresentation() {
        return transRepr;
    }

    @Override
    public DD computeSignatures(DD partitions) {
        DD partitionsNext = partitions.permute(original.getSwapPresNext());
        DD equalsPreviousBlock = pVar.multiply(partitions);

        // The maximum probability to reach a block
        DD blockTransMax = reachStatesMax.multiply(partitionsNext)
                .abstractMaxWith(original.getNextCube().clone());
        // The minimum probability to reach a block
        // We also use abstractMax here because the primes representing the probabilities are reversed
        DD blockTransMin = reachStatesMin.multiply(partitionsNext)
                .abstractMaxWith(original.getNextCube().clone());
        partitionsNext.dispose();

        // The leafs have prime numbers so collisions are not possible
        DD result = blockTransMax.multiplyWith(blockTransMin.permuteWith(blockPresNextPerm))
                .addWith(equalsPreviousBlock);
        return result;
    }
}
