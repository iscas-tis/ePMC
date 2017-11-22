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

package epmc.graph;

import static epmc.error.UtilError.ensure;

import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graph.options.OptionsGraph;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitStoreableToNumber;
import epmc.util.UtilBitSet;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class UtilGraph {

    public static BitStoreableToNumber newNodeStore(int numBits) {
        BitStoreableToNumber nodeStore;
        OptionsTypesGraph.StateStorage stateStorage;
        stateStorage = Options.get().getEnum(OptionsGraph.STATE_STORAGE);
        ensure((stateStorage != OptionsTypesGraph.StateStorage.INT || numBits <= Integer.SIZE)
                && (stateStorage != OptionsTypesGraph.StateStorage.LONG || numBits <= Long.SIZE),
                ProblemsGraph.STATE_DS_TOO_SMALL);
        if (stateStorage == OptionsTypesGraph.StateStorage.SMALLEST) {
            if (numBits <= Integer.SIZE) {
                stateStorage = OptionsTypesGraph.StateStorage.INT;
            } else if (numBits <= Long.SIZE) {
                stateStorage = OptionsTypesGraph.StateStorage.LONG;
            } else if (numBits < Integer.MAX_VALUE) {
                stateStorage = OptionsTypesGraph.StateStorage.LONG_ARRAY;
            } else {
                assert false;
            }
        }
        switch (stateStorage) {
        case INT:
            nodeStore = BitStoreableToNumber.newNodeStoreInt();
            break;
        case LONG:
            nodeStore = BitStoreableToNumber.newNodeStoreLong();
            break;
        case LONG_ARRAY:
            nodeStore = BitStoreableToNumber.newNodeStoreLongArray(numBits);
            break;
        default:
            assert false;
            nodeStore = null;
            break;
        }
        return nodeStore;
    }

    public static StateSetDD computeAllStatesDD(GraphDD graphDD) {
        if (graphDD == null) {
            return null;
        }
        DD statesDD = graphDD.getNodeProperty(CommonProperties.STATE);
        statesDD = statesDD.and(graphDD.getNodeSpace());
        return new StateSetDD(graphDD, statesDD);
    }

    public static StateSetExplicit computeAllStatesExplicit(GraphExplicit explicit) {
        if (explicit == null) {
            return null;
        }
        explicit.explore();
        BitSet statesBs = UtilBitSet.newBitSetUnbounded();
        NodeProperty isState = explicit.getNodeProperty(CommonProperties.STATE);
        for (int node = 0; node < explicit.getNumNodes(); node++) {
            if (isState.getBoolean(node)) {
                statesBs.set(node);
            }
        }
        return new StateSetExplicit(explicit, statesBs);
    }

    public static StateMapExplicit newStateMap(StateSetExplicit states, ValueArray valuesExplicit) {
        assert states != null;
        assert valuesExplicit != null;
        return new StateMapExplicit(states, valuesExplicit);
    }

    /**
     * Register a result resulting from checking subformula.
     * Values contained in a state map will be converted into a node property.
     * None of the parameters may be {@code null}.
     * 
     * @param lowLevel graph to register result in
     * @param property subformula to register result of
     * @param results result resulting from checking subformula
     */
    public static void registerResult(GraphExplicit lowLevel, Expression property, StateMapExplicit results) {
        assert lowLevel != null;
        assert property != null;
        assert results != null;
        if (!lowLevel.getNodeProperties().contains(property)) {
            lowLevel.addSettableNodeProperty(property, results.getType());
        }
        NodeProperty nodeProp = lowLevel.getNodeProperty(property);
        Value entry = results.getType().newValue();
        int resultsSize = results.size();
        NodeProperty stateProperty = lowLevel.getNodeProperty(CommonProperties.STATE);
        for (int i = 0; i < resultsSize; i++) {
            results.getExplicitIthValue(entry, i);
            int state = results.getExplicitIthState(i);
            nodeProp.set(state, entry);
            int numSuccessors = lowLevel.getNumSuccessors(state);
            for (int succ = 0; succ < numSuccessors; succ++) {
                int succNode = lowLevel.getSuccessorNode(state, succ);
                if (!stateProperty.getBoolean(succNode)) {
                    nodeProp.set(succNode, entry);
                }
            }
        }
    }

}
