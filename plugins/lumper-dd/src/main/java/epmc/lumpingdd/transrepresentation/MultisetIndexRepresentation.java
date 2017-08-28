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

package epmc.lumpingdd.transrepresentation;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.dd.Walker;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.LumperDDSignature;
import epmc.value.Value;

/**
 * This class can translate a transition relation
 * with doubles to a transition relation with only
 * integers, preventing floating point errors.
 * It keeps a list of all the transition values
 * occurring in the original transition relation.
 * The additional variables indicate the index in that
 * list. The leafs of the ADD indicate the multiplicity
 * of that element.
 */
public class MultisetIndexRepresentation implements TransitionRepresentation {

    private final static String MULTISET_INDEX = "%multiset_index";

    private VariableDD multisetVarDD;
    private GraphDD original;
    private ContextDD contextDD;
    private boolean isNonDet;
    private Value[] weights;
    private DD resultCache;

    @Override
    public void setOriginal(GraphDD original) {
        this.original = original;
        this.contextDD = original.getContextDD();
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        isNonDet = SemanticsNonDet.isNonDet(semantics);
    }

    @Override
    public DD fromTransWeights() {
        return fromTransWeightsWithActions().abstractSum(original.getActionCube());
    }

    private DD fromTransWeightsWithActions() {
        if(resultCache != null) {
            return resultCache;
        }

        DD edgeWeights = original.getEdgeProperty(CommonProperties.WEIGHT);
        Set<Value> weightSet = new HashSet<>();
        contextDD.collectValues(weightSet, edgeWeights, contextDD.newConstant(true));
        weightSet.remove(null);
        weights = new Value[weightSet.size()];
        weightSet.toArray(weights);

        multisetVarDD = contextDD.newInteger(MULTISET_INDEX, isNonDet ? 2 : 1, 0, weights.length - 1);
        Map<Value, DD> valueReplacements = new HashMap<>();
        for (int i = 0; i < weights.length; i++) {
            valueReplacements.put(weights[i], multisetVarDD.newIntValue(0, i));
        }
        // and with the boolean transition relation to take 0 out of the multiset
        // we are not interested in actions
        TLongObjectMap<DD> cache = new TLongObjectHashMap<>();
        resultCache = replaceSymbols(contextDD, edgeWeights.walker(), valueReplacements, cache)
                .and(original.getTransitions()).toMTWith();
        LumperDDSignature.disposeCachedDDs(cache);
        return resultCache;
    }

    /**
     * Replace the leafs by DDs according to the relation
     * in replacements.
     */
    private DD replaceSymbols(ContextDD contextDD, Walker w, Map<Value, DD> replacements, TLongObjectMap<DD> computedCache) {
        if(computedCache.containsKey(w.uniqueId())) {
            return computedCache.get(w.uniqueId());
        }

        DD result;
        if(w.isLeaf()) {
            result = replacements.get(w.value());
        } else {
            w.low();
            DD low = replaceSymbols(contextDD, w, replacements, computedCache);
            w.back();
            w.high();
            DD high = replaceSymbols(contextDD, w, replacements, computedCache);
            w.back();
            DD thisNode = contextDD.variable(w.variable());
            result = thisNode.ite(high, low);
        }
        computedCache.put(w.uniqueId(), result);
        return result;
    }
}
