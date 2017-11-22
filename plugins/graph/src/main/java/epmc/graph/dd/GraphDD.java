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

package epmc.graph.dd;

import java.math.BigInteger;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.expression.Expression;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

public interface GraphDD extends LowLevel {
    /* methods to be implemented by implementing classes. */

    DD getInitialNodes();

    DD getTransitions();

    DD getPresCube();

    DD getNextCube();

    DD getActionCube();

    /* TODO get rid of following method */

    Permutation getSwapPresNext();

    DD getNodeSpace();

    @Override
    void close();

    GraphDDProperties getProperties();

    default <T> T getGraphPropertyObject(Object property) {
        ValueObject graphProperty = ValueObject.as(getGraphProperty(property));
        if (graphProperty == null) {
            return null;
        } else {
            return graphProperty.getObject();
        }
    }

    default void registerGraphProperty(Object property, Type type) {
        getProperties().registerGraphProperty(property, type);
    }

    default Value getGraphProperty(Object property) {
        return getProperties().getGraphProperty(property);
    }

    default DD getNodeProperty(Object property) {
        return getProperties().getNodeProperty(property);
    }

    default Set<Object> getNodeProperties() {
        return getProperties().getNodeProperties();
    }

    default DD getEdgeProperty(Object property) {
        return getProperties().getEdgeProperty(property);
    }

    default Set<Object> getGraphProperties() {
        return getProperties().getGraphProperties();
    }

    default void setGraphPropertyObject(Object property, Object value) {
        getProperties().setGraphPropertyObject(property, value);
    }

    default void setGraphProperty(Object property, Value value) {
        getProperties().setGraphProperty(property, value);
    }

    default void registerNodeProperty(Object property, DD value) {
        getProperties().registerNodeProperty(property, value);
    }

    default void registerEdgeProperty(Object property, DD value) {
        getProperties().registerEdgeProperty(property, value);
    }

    default ContextDD getContextDD() {
        return ContextDD.get();
    }

    @Override
    default StateSet newInitialStateSet() {
        return new StateSetDD(this, getInitialNodes().clone());
    }

    default BigInteger getNumNodes() {
        return getNodeSpace().countSat(getPresCube());
    }

    @Override
    default Type getType(Expression expression) {
        assert expression != null;
        Value graphProperty = getGraphProperty(expression);
        if (graphProperty != null) {
            return graphProperty.getType();
        }
        DD nodeProperty = getNodeProperty(expression);
        if (nodeProperty != null) {
            return nodeProperty.getType();
        }
        DD edgeProperty = getEdgeProperty(expression);
        if (edgeProperty != null) {
            return edgeProperty.getType();
        }
        return null;
    }
}
