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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.operator.Operator;
import epmc.operator.OperatorIte;
import epmc.util.BitSet;
import epmc.util.HashingStrategyArrayLong;
import epmc.util.UtilBitSet;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;

/**
 * Class to allow to execute operations not supported by a certain DD library.
 * Most MTBDD libraries are restricted to executing unary and binary operations,
 * plus the ternary if-then-else operation, but usually do not support other
 * ternary operations or operations with four or more operands. To support such
 * operators anyway, this class provides a generic apply function with an
 * unlimited number of arguments.
 * Such an apply function would be needed e.g. if we would allow symbolic
 * solving of quantum Markov chains. To construct arrays, an operator with an
 * arbitrary number of arguments is required.
 * 
 * TODO this thing has not been used in the past, and might not work correctly in all cases
 * 
 * @author Ernst Moritz Hahn
 */
final class GenericOperations {

    /**
     * Cache storing results for a given low-level DD library and given operator.
     * 
     * @author Ernst Moritz Hahn
     */
    private final static class Cache {
        /** Maps from operands to results of operation. */
        private final Object2LongOpenCustomHashMap<long[]> content;
        /** Low-level DD library this cache belongs to. */
        private final LibraryDD lowLevel;

        Cache(LibraryDD lowLevel) {
            content = new Object2LongOpenCustomHashMap<>(HashingStrategyArrayLong.getInstance());
            content.defaultReturnValue(-1L);
            this.lowLevel = lowLevel;
        }

        boolean contains(long... entry) {
            assert entry != null;
            return content.containsKey(entry);
        }

        long get(long... entry) {
            assert entry != null;
            return content.getLong(entry);
        }

        /**
         * Store the according cached result.
         * The entry array will be cloned before storing.
         * 
         * @param entry
         * @param value
         */
        void put(long[] entry, long value) {
            assert entry != null;
            assert !content.containsKey(entry);
            content.put(entry.clone(), value);
        }

        public void clear() {
            content.forEach((key,value) -> lowLevel.free(value));
            content.clear();
        }
    }

    /**
     * Maintains a cache pairs of DD library and operator.
     * Thus, if a certain operator shall be executed, the first step is to
     * obtain cache corresponding to the current combination of DD library and
     * operator. This cache can then be used in the subsequent steps to store
     * the result and the intermediate steps.
     * 
     * @author Ernst Moritz Hahn
     */
    private final static class Caches {
        /** Map from DD libraries to map of operators to caches. */
        private final Map<LibraryDD, Map<OperatorEvaluator,Cache>> caches;

        Caches() {
            caches = new HashMap<>();
        }

        Cache get(LibraryDD lowLevel, OperatorEvaluator operation) {
            assert lowLevel != null;
            assert operation != null;
            Map<OperatorEvaluator,Cache> lowLevelCaches = caches.get(lowLevel);
            if (lowLevelCaches == null) {
                lowLevelCaches = new HashMap<>();
                caches.put(lowLevel, lowLevelCaches);
            }
            Cache result = lowLevelCaches.get(operation);
            if (result == null) {
                result = new Cache(lowLevel);
                lowLevelCaches.put(operation, result);
            }
            return result;
        }
    }

    private final static int LEAF_REACHED = Integer.MAX_VALUE;
    private final ContextDD contextDD;
    private final Map<LibraryDD, List<DD>> llVariables;
    private final Caches caches = new Caches();

    /**
     * Create new generic operations object.
     * The DD context parameter may not be {@code null}.
     * 
     * @param contextDD DD context which this object shall support
     */
    GenericOperations(ContextDD contextDD) {
        assert contextDD != null;
        this.contextDD = contextDD;
        this.llVariables = contextDD.getLowLevelVariables();
    }

    long apply(OperatorEvaluator operator, Operator identifier, Type type, LibraryDD lowLevel, long... operands)
    {
        assert operator != null;
        assert lowLevel != null;
        assert lowLevel.getContextDD() == contextDD;
        assert operands != null;
        Walker[] walkers = new Walker[operands.length];
        for (int index = 0; index < operands.length; index++) {
            walkers[index] = new Walker(null, lowLevel, operands[index], true);
        }
        Cache cache = caches.get(lowLevel, operator);
        long[] entry = new long[operands.length];
        BitSet[] back = new BitSet[contextDD.numVariables()];
        for (int entryNr = 0; entryNr < back.length; entryNr++) {
            back[entryNr] = UtilBitSet.newBitSetUnbounded();
        }
        long result = lowLevel.clone(apply(operator, identifier, type, lowLevel, cache, entry, walkers, 0, back));
        // if it turns out that this class is used for expensive operations,
        // make cache persistent
        cache.clear();
        return result;
    }

    /**
     * Internal apply function.
     * 
     * The cache entry parameter is given as a parameter to avoid having to
     * allocate a new array in each recursive step.
     * 
     * @param operator operator to apply
     * @param type result type of the operation
     * @param libraryDD DD library to use
     * @param cache cache to use
     * @param cacheEntry array to use for looking up cache values
     * @param operands
     * @param recursionDepth 
     * @param back2 
     * @return
     */
    private long apply(OperatorEvaluator operator, Operator identifier, Type type, LibraryDD libraryDD,
            Cache cache, long[] cacheEntry, Walker[] operands, int recursionDepth, BitSet[] backSets)
    {
        /* Check whether result has already been compute before. */
        for (int index = 0; index < operands.length; index++) {
            cacheEntry[index] = operands[index].uniqueId();
        }
        if (cache.contains(cacheEntry)) {
            return cache.get(cacheEntry);
        }

        int lowestVar = LEAF_REACHED;
        for (Walker dd : operands) {
            // TODO adapt once we implement reordering
            int var = dd.isLeaf() ? LEAF_REACHED : dd.variable();
            lowestVar = Math.min(lowestVar, var);
        }

        long result ;
        if (lowestVar == LEAF_REACHED) {
            /* If all walkers have reached a leaf node, we can compute the
             * terminal constant node. */
            Value[] leafValues = new Value[operands.length];
            for (int index = 0; index < operands.length; index++) {
                leafValues[index] = operands[index].value();
            }
            Value resultValue = type.newValue();
            operator.apply(resultValue, leafValues);
            result = libraryDD.newConstant(resultValue);
        } else {
            BitSet back = backSets[recursionDepth];
            for (int index = 0; index < operands.length; index++) {
                Walker dd = operands[index];
                int var = dd.isLeaf() ? LEAF_REACHED : dd.variable();
                if (var == lowestVar) {
                    dd.high();
                    back.set(index);
                }
            }
            long highResult = apply(operator, identifier, type, libraryDD, cache, cacheEntry, operands, recursionDepth + 1, backSets);
            for (int index = 0; index < operands.length; index++) {
                Walker dd = operands[index];
                if (back.get(index)) {
                    dd.back();
                }
            }
            for (int index = 0; index < operands.length; index++) {
                Walker dd = operands[index];
                int var = dd.isLeaf() ? LEAF_REACHED : dd.variable();
                if (var == lowestVar) {
                    dd.low();
                    back.set(index);
                }
            }
            long lowResult = apply(operator, identifier, type, libraryDD, cache, cacheEntry, operands, recursionDepth + 1, backSets);
            for (int index = 0; index < operands.length; index++) {
                Walker dd = operands[index];
                if (back.get(index)) {
                    dd.back();
                }
                back.set(index, false);
            }
            long variable = llVariables.get(libraryDD).get(lowestVar).uniqueId();
            result = libraryDD.apply(OperatorIte.ITE, type, variable, highResult, lowResult);
        }
        for (int index = 0; index < operands.length; index++) {
            cacheEntry[index] = operands[index].uniqueId();
        }
        cache.put(cacheEntry, result);
        return result;
    }
}
