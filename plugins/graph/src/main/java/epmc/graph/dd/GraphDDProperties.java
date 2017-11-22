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

import java.io.Closeable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

public final class GraphDDProperties implements Closeable {
    private final Map<Object,Value> graphProperties = new LinkedHashMap<>();
    private final Map<Object,Value> graphPropertiesExternal = Collections.unmodifiableMap(graphProperties);
    private final Map<Object,DD> nodeProperties = new LinkedHashMap<>();
    private final Map<Object,DD> nodePropertiesExternal = Collections.unmodifiableMap(nodeProperties);
    private final Map<Object,DD> edgeProperties = new LinkedHashMap<>();
    private final Map<Object,DD> edgePropertiesExternal = Collections.unmodifiableMap(edgeProperties);
    private GraphDD graph;
    private boolean closed;

    public GraphDDProperties(GraphDD graph) {
        assert graph != null;
        this.graph = graph;
    }

    public final Set<Object> getGraphProperties() {
        return graphPropertiesExternal.keySet();
    }

    public final Value getGraphProperty(Object property) {
        assert property != null;
        return graphProperties.get(property);
    }


    public final void registerGraphProperty(Object propertyName, Type type) {
        assert propertyName != null;
        assert type != null;
        assert !graphProperties.containsKey(propertyName) : propertyName;
        graphProperties.put(propertyName, type.newValue());
    }

    public final void setGraphProperty(Object property, Value value) {
        assert property != null;
        assert value != null;
        assert graphProperties.containsKey(property);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, value.getType(), graphProperties.get(property).getType());
        set.apply(graphProperties.get(property), value);
    }


    public final void registerNodeProperty(Object propertyName, DD property) {
        assert propertyName != null;
        assert property != null;
        if (nodeProperties.containsKey(propertyName)) {
            return;
        }
        nodeProperties.put(propertyName, property.clone());
    }

    public final DD getNodeProperty(Object property) {
        assert property != null;
        return nodeProperties.get(property);
    }

    public final Set<Object> getNodeProperties() {
        return nodePropertiesExternal.keySet();
    }

    public final void registerEdgeProperty(Object propertyName,
            DD property) {
        assert propertyName != null;
        assert property != null;
        if (edgeProperties.containsKey(propertyName)) {
            return;
        }
        edgeProperties.put(propertyName, property.clone());
    }


    public final DD getEdgeProperty(Object property) {
        assert property != null;
        return edgeProperties.get(property);
    }

    public final Set<Object> getEdgeProperties() {
        return edgePropertiesExternal.keySet();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (DD nodeProperty : nodeProperties.values()) {
            nodeProperty.dispose();
        }
        for (DD edgeProperty : edgeProperties.values()) {
            edgeProperty.dispose();
        }
    }

    public GraphDD getGraph() {
        return graph;
    }

    public ContextDD getContextDD() {
        return graph.getContextDD();
    }

    public void setGraphProperty(Object property, Object value) {
        ValueObject.as(getGraphProperty(property)).set(value);
    }

    public void removeGraphProperty(Object property) {
        graphProperties.remove(property);
    }

    public void removeNodeProperty(Object property) {
        nodeProperties.get(property).dispose();
        nodeProperties.remove(property);
    }

    public void removeEdgeProperty(Object property) {
        edgeProperties.get(property).dispose();
        edgeProperties.remove(property);
    }

    public void setGraphPropertyObject(Object property, Object value) {
        ValueObject.as(graphProperties.get(property)).set(value);
    }    
}
