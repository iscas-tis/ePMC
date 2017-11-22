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

package epmc.jani.explorer;

import java.util.Arrays;

import epmc.graph.explorer.Explorer;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

/**
 * Explorer edge property for JANI explorers and their components.
 * Note that this class stores values only temporarily for a given call of
 * {@link Explorer#queryNode(epmc.graph.ExplorerNode)}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertyEdgeGeneral implements PropertyEdge {
    /** The explorer to which this property belongs. */
    private final Explorer explorer;
    /** Type of values stored. */
    private final Type type;
    /** Value of this property for the successors of the node queried last. */
    private Value[] values;
    /** Used to return value of successors. */
    private final Value value;
    private final OperatorEvaluator set;

    /**
     * Construct new edge property.
     * None of the parameters may be {@code null}.
     * 
     * @param explorer explorer to which the property shall belong to
     * @param type type of the property
     */
    public PropertyEdgeGeneral(Explorer explorer, Type type) {
        assert explorer != null;
        assert type != null;
        this.explorer = explorer;
        this.type = type;
        this.value = type.newValue();
        this.values = new Value[1];
        this.values[0] = type.newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, type, type);
    }

    /**
     * Set value of given successor
     * The parameter may not be {@code null}. The successor parameter must not
     * be negative. The number of successors available will be increased if
     * necessary.
     * 
     * @param successor successor to set value for
     * @param value value to set
     */
    public void set(int successor, Value value) {
        assert value != null;
        ensureSuccessorsSize(successor);
        set.apply(values[successor], value);
    }

    /**
     * Set object value of given successor
     * The property type must be an object type for this function to be allowed
     * to be called. The successor parameter must not be negative. The number of
     * successors available will be increased if necessary.
     * 
     * @param successor successor to set value for
     * @param value value to set
     */
    public void set(int successor, Object value) {
        assert value != null;
        ensureSuccessorsSize(successor);
        ValueObject.as(values[successor]).set(value);
    }

    @Override
    public Value get(int successor) {
        assert successor >= 0;
        assert successor < explorer.getNumSuccessors();
        ensureSuccessorsSize(successor);
        set.apply(value, values[successor]);
        return value;
    }

    /**
     * Ensure that given successor can be stored successfully.
     * For this, the array storing successor values will be increased as
     * necessary.
     * 
     * @param successor number of successor the value of which to be  stored
     */
    private void ensureSuccessorsSize(int successor) {
        int numSuccessors = successor + 1;
        if (numSuccessors < values.length) {
            return;
        }
        int newLength = values.length;
        while (newLength <= numSuccessors) {
            newLength *= 2;
        }
        Value[] newValues = Arrays.copyOf(values, newLength);
        for (int newSuccNr = values.length; newSuccNr < newValues.length; newSuccNr++) {
            newValues[newSuccNr] = type.newValue();
        }
        values = newValues;
    }

    @Override
    public Type getType() {
        return type;
    }
}
