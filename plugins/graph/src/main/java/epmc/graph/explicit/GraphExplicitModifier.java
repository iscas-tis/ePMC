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

package epmc.graph.explicit;

import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operator.OperatorMax;

public final class GraphExplicitModifier {    
    public static void embed(GraphExplicit graph) {
        assert graph != null;
        ValueAlgebra zero = TypeWeight.get().getZero();
        ValueAlgebra sum = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            Player player = playerProp.getEnum(node);
            if (player == Player.STOCHASTIC) {
                sum.set(zero);
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    sum.add(sum, weightProp.get(node, succNr));
                }
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    weight.set(weightProp.get(node, succNr));
                    weight.divide(weight, sum);
                    weightProp.set(node, succNr, weight);
                }
            }
        }
    }

    public static void uniformise(GraphExplicit graph, Value uniRate) {
        assert graph != null;
        Value uniformisationRate = computeUniformisationRate(graph);
        if (uniRate != null) {
            uniRate.set(uniformisationRate);
        }
        ValueAlgebra zero = TypeWeight.get().getZero();
        ValueAlgebra sum = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            Player player = playerProp.getEnum(node);
            if (player == Player.STOCHASTIC) {
                sum.set(zero);
                for (int succNr = 0; succNr < graph.getNumSuccessors(node) - 1; succNr++) {
                    sum.add(sum, weightProp.get(node, succNr));
                }
                for (int succNr = 0; succNr < graph.getNumSuccessors(node) - 1; succNr++) {
                    weight.set(weightProp.get(node, succNr));
                    weight.divide(weight, uniformisationRate);
                    weightProp.set(node, succNr, weight);
                }
                weight.subtract(uniformisationRate, sum);
                weight.divide(weight, uniformisationRate);
                weightProp.set(node, graph.getNumSuccessors(node) - 1, weight);
                graph.setSuccessorNode(node, graph.getNumSuccessors(node) - 1, node);
            }
        }
    }
    
    private static Value computeUniformisationRate(GraphExplicit graph)
            {
        ValueAlgebra result = newValueWeight();
        ValueAlgebra sumRate = newValueWeight();
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        OperatorEvaluator max = ContextValue.get().getOperatorEvaluator(OperatorMax.MAX, result.getType(), sumRate.getType());
        for (int inputNode = 0; inputNode < graph.getNumNodes(); inputNode++) {
            Player player = playerProp.getEnum(inputNode);
            if (player == Player.STOCHASTIC) {
                sumRate.set(0);
                int numSuccs = graph.getNumSuccessors(inputNode) - 1;
                for (int succNr = 0; succNr < numSuccs; succNr++) {
                    Value rate = weight.get(inputNode, succNr);
                    sumRate.add(sumRate, rate);
                }
                max.apply(result, result, sumRate);
            }
        }
        return result;
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private GraphExplicitModifier() {
    }
    
    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
