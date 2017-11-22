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

import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

/**
 * General node property.
 * This node property can be used for an explicit-state graph, for any type of
 * value. All values of the property have to be set explicitly.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NodePropertyGeneral implements NodeProperty {
    /** Graph to which this property belogns. */
    private final GraphExplicit graph;
    /** Value returned by {@link #get()}. */
    private final Value value;
    /** Array value storing the values for all nodes so far. */
    private ValueArray content;
    /** Value for nodes for which no value was set. */
    private final Value defaultValue;

    /**
     * Construct new general node property.
     * The type of the node property is derived from the default value.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which the property shall belong
     * @param defaultValue value for nodes for which no value was set
     */
    public NodePropertyGeneral(GraphExplicit graph, Value defaultValue) {
        assert graph != null;
        assert defaultValue != null;
        Type type = defaultValue.getType();
        this.graph = graph;
        this.value = type.newValue();
        this.defaultValue = UtilValue.clone(defaultValue);
        TypeArray typeArray = type.getTypeArray();
        this.content = UtilValue.newArray(typeArray, 1);
    }

    /**
     * Construct new general node property.
     * The default value used is the one obtained by {@link Type#newValue()}.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which the property shall belong
     * @param type type of the node property
     */
    public NodePropertyGeneral(GraphExplicit graph, Type type) {
        assert graph != null;
        assert type != null;
        this.graph = graph;
        this.value = type.newValue();
        this.defaultValue = type.newValue();
        TypeArray typeArray = type.getTypeArray();
        if (typeArray == null) {
            typeArray = type.getTypeArray();
        }
        this.content = UtilValue.newArray(typeArray, 1);
    }

    @Override
    public Value get(int node) {
        ensureSize(node);
        content.get(value, node);
        return value;
    }

    @Override
    public void set(int node, Value value) {
        ensureSize(node);
        content.set(value, node);
    }

    @Override
    public void set(int node, Object object) {
        assert object != null;
        assert ValueObject.is(value);
        ensureSize(node);
        ValueObject.as(value).set(object);
        content.set(value, node);
    }    

    @Override
    public void set(int node, Enum<?> object) {
        assert object != null;
        assert ValueEnum.is(value);
        ensureSize(node);
        ValueEnum.as(value).set(object);
        content.set(value, node);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    /**
     * Extends the size of the array storing node values, if necessary.
     * The new size will be at least as large as queriedNode + 1, ensuring that a value for
     * the queried node can be stored or read.
     * This function should be called before any operation reading or storing
     * node values to ensure {@link #content} is large enough.
     */
    private void ensureSize(int queriedNode) {
        int oldSize = content.size();
        content = UtilValue.ensureSize(content, queriedNode + 1);
        for (int i = oldSize; i < content.size(); i++) {
            content.set(defaultValue, i);
        }
    }
}
