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

package epmc.graph.explorer;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;

import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

// TODO integrate this class into explorer stuff

public final class ExplorerProperties implements Serializable {
    /** IL, because I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** String "explorerProperties". */
    private final static String EXPLORER_PROPERTIES = "explorerProperties";
    /** String "nodeProperties". */
    private final static String NODE_PROPERTIES = "nodeProperties";
    /** String "edgeProperties". */
    private final static String EDGE_PROPERTIES = "edgeProperties";

    private final Map<Object,Value> explorerProperties = new LinkedHashMap<>();
    private final Map<Object,Value> explorerPropertiesExternal = Collections.unmodifiableMap(explorerProperties);
    private final Map<Object,ExplorerNodeProperty> nodeProperties = new LinkedHashMap<>();
    private final Map<Object,ExplorerNodeProperty> nodePropertiesExternal = Collections.unmodifiableMap(nodeProperties);
    private final Map<Object,ExplorerEdgeProperty> edgeProperties = new LinkedHashMap<>();
    private final Map<Object,ExplorerEdgeProperty> edgePropertiesExternal = Collections.unmodifiableMap(edgeProperties);

    public ExplorerProperties(Explorer explorer) {
        assert explorer != null;
    }

    // TODO maybe this method should be removed later
    public Set<Object> getExplorerProperties() {
        return explorerPropertiesExternal.keySet();
    }

    public Map<Object,Value> getExplorerPropertiesMap() {
        return explorerPropertiesExternal;
    }

    public Value getExplorerProperty(Object property) {
        assert property != null;
        return explorerProperties.get(property);
    }

    public void registerExplorerProperty(Object propertyName, Value value) {
        assert propertyName != null;
        assert value != null;
        assert !explorerProperties.containsKey(propertyName);
        explorerProperties.put(propertyName, UtilValue.clone(value));
    }

    public void registerExplorerProperty(Object propertyName, Type type) {
        assert propertyName != null;
        assert type != null;
        assert !explorerProperties.containsKey(propertyName);
        explorerProperties.put(propertyName, type.newValue());
    }

    public void setExplorerProperty(Object property, Value value) {
        assert property != null;
        assert value != null;
        assert explorerProperties.containsKey(property);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, value.getType(), getExplorerProperty(property).getType());
        set.apply(getExplorerProperty(property), value);
    }


    public void registerNodeProperty(Object propertyName,
            ExplorerNodeProperty property) {
        assert propertyName != null;
        assert property != null;
        if (nodeProperties.containsKey(propertyName)) {
            return;
        }
        nodeProperties.put(propertyName, property);
    }

    public ExplorerNodeProperty getNodeProperty(Object property) {
        assert property != null;
        return nodeProperties.get(property);
    }

    // TODO maybe this method should be removed later
    public Set<Object> getNodeProperties() {
        return nodePropertiesExternal.keySet();
    }

    public Map<Object,ExplorerNodeProperty> getNodePropertiesMap() {
        return nodePropertiesExternal;
    }

    public void registerEdgeProperty(Object propertyName,
            ExplorerEdgeProperty property) {
        assert propertyName != null;
        assert property != null;
        if (edgeProperties.containsKey(propertyName)) {
            return;
        }
        edgeProperties.put(propertyName, property);
    }

    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        assert property != null;
        return edgeProperties.get(property);
    }

    // TODO maybe this method should be removed later
    public Set<Object> getEdgeProperties() {
        return edgePropertiesExternal.keySet();
    }

    public Map<Object,ExplorerEdgeProperty> getEdgePropertiesMap() {
        return edgePropertiesExternal;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(EXPLORER_PROPERTIES, explorerProperties)
                .add(NODE_PROPERTIES, nodeProperties)
                .add(EDGE_PROPERTIES, edgeProperties)
                .toString();
    }

    public void removeExplorerProperty(Object property) {
        explorerProperties.remove(property);
    }

    public void removeNodeProperty(Object property) {
        nodeProperties.remove(property);
    }

    public void removeEdgeProperty(Object property) {
        edgeProperties.remove(property);
    }
}
