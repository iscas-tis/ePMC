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

package epmc.graphsolver.objective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import epmc.expression.Expression;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyExpression;
import epmc.value.ValueArray;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class GraphSolverObjectiveExplicitLump implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private List<Expression> atomics;
    private NodeProperty[] nodeProperties;
    private Int2IntOpenHashMap blockToNumberInt;
    private int numStates;

    @Override
    public void setGraph(GraphExplicit graph) {
        this.graph = graph;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public void setResult(ValueArray result) {
    }

    @Override
    public ValueArray getResult() {
        return null;
    }

    public void setAtomics(Collection<Expression> atomics) {
        assert atomics != null;
        for (Expression expression : atomics) {
            assert expression != null;
        }
        this.atomics = new ArrayList<>(atomics);
    }

    public void prepare() {
        this.nodeProperties = computeAtomNodeProperties(graph, atomics);
        this.blockToNumberInt = new Int2IntOpenHashMap();
        this.blockToNumberInt.defaultReturnValue(-1);
        int numStates = graph.computeNumStates();
        for (int state = 0; state < numStates; state++) {
            int block = 0;
            int marker = 1;
            for (int atomicNr = 0; atomicNr < nodeProperties.length; atomicNr++) {
                if (nodeProperties[atomicNr].getBoolean(state)) {
                    block |= marker;
                }
                marker <<= 1;
            }
            int number = blockToNumberInt.get(block);
            if (number == -1) {
                number = blockToNumberInt.size();
                blockToNumberInt.put(block, number);
            }
        }
        this.numStates = numStates;
    }	

    public int getBlock(int state) {
        int block = 0;
        int marker = 1;
        for (int atomicNr = 0; atomicNr < nodeProperties.length; atomicNr++) {
            if (nodeProperties[atomicNr].getBoolean(state)) {
                block |= marker;
            }
            marker <<= 1;
        }
        return blockToNumberInt.get(block);
    }

    public int size() {
        return numStates;
    }

    private static NodeProperty[] computeAtomNodeProperties(GraphExplicit graph,
            Collection<Expression> atomics) {
        List<NodeProperty> result = new ArrayList<>();
        for (Expression atomic : atomics) {
            if (graph.getNodeProperty(atomic) != null) {
                result.add(graph.getNodeProperty(atomic));
            } else {
                result.add(new NodePropertyExpression(graph, atomic));
            }
        }
        return result.toArray(new NodeProperty[0]);
    }
}
