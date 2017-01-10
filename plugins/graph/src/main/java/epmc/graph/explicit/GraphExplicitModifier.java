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

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.value.ContextValue;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public final class GraphExplicitModifier {    
    public static void embed(GraphExplicit graph) throws EPMCException {
        assert graph != null;
        ContextValue contextValue = graph.getContextValue();
        ValueAlgebra zero = TypeWeight.get(contextValue).getZero();
        ValueAlgebra sum = newValueWeight(contextValue);
        ValueAlgebra weight = newValueWeight(contextValue);
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            graph.queryNode(node);
            Player player = playerProp.getEnum();
            if (player == Player.STOCHASTIC) {
                sum.set(zero);
                for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                    sum.add(sum, weightProp.get(succNr));
                }
                for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                    weight.set(weightProp.get(succNr));
                    weight.divide(weight, sum);
                    weightProp.set(weight, succNr);
                }
            }
        }
    }

    public static void uniformise(GraphExplicit graph, Value uniRate) throws EPMCException {
        assert graph != null;
        Value uniformisationRate = computeUniformisationRate(graph);
        if (uniRate != null) {
            uniRate.set(uniformisationRate);
        }
        ContextValue contextValue = graph.getContextValue();
        ValueAlgebra zero = TypeWeight.get(contextValue).getZero();
        ValueAlgebra sum = newValueWeight(contextValue);
        ValueAlgebra weight = newValueWeight(contextValue);
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            graph.queryNode(node);
            Player player = playerProp.getEnum();
            if (player == Player.STOCHASTIC) {
                sum.set(zero);
                for (int succNr = 0; succNr < graph.getNumSuccessors() - 1; succNr++) {
                    sum.add(sum, weightProp.get(succNr));
                }
                for (int succNr = 0; succNr < graph.getNumSuccessors() - 1; succNr++) {
                    weight.set(weightProp.get(succNr));
                    weight.divide(weight, uniformisationRate);
                    weightProp.set(weight, succNr);
                }
                weight.subtract(uniformisationRate, sum);
                weight.divide(weight, uniformisationRate);
                weightProp.set(weight, graph.getNumSuccessors() - 1);
                graph.setSuccessorNode(graph.getNumSuccessors() - 1, node);
            }
        }
    }
    
    private static Value computeUniformisationRate(GraphExplicit graph)
            throws EPMCException {
        ContextValue contextValue = graph.getContextValue();
        ValueAlgebra result = newValueWeight(contextValue);
        ValueAlgebra sumRate = newValueWeight(contextValue);
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int inputNode = 0; inputNode < graph.getNumNodes(); inputNode++) {
            Player player = playerProp.getEnum();
            if (player == Player.STOCHASTIC) {
                sumRate.set(0);
                graph.queryNode(inputNode);
                int numSuccs = graph.getNumSuccessors() - 1;
                for (int succNr = 0; succNr < numSuccs; succNr++) {
                    Value rate = weight.get(succNr);
                    sumRate.add(sumRate, rate);
                }
                result.max(result, sumRate);
            }
        }
        return result;
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private GraphExplicitModifier() {
    }
    
    private static ValueAlgebra newValueWeight(ContextValue contextValue) {
        return TypeWeight.get(contextValue).newValue();
    }
}
