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

package epmc.jani.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * Class storing the actions of a model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Actions implements JANINode, Map<String,Action>, Iterable<Action> {
    /** Unique map of this actions set. */
    private final Map<String,Action> actions = new LinkedHashMap<>();
    private ModelJANI model;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        JsonArray array = UtilJSON.toArrayObject(value);
        for (JsonValue act : array) {
            Action action = new Action();
            action.setModel(model);
            action.parse(act);
            UtilJSON.ensureUnique(action.getName(), actions);
            actions.put(action.getName(), action);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Action action : actions.values()) {
            if (!model.getSilentAction().equals(action))
                result.add(action.generate());
        }
        return result.build();
    }

    /**
     * Get map of action names to actions.
     * The map returned is unmodifiable. This method may only be called after
     * the object has been parsed.
     * 
     * @return map of action names to actions.
     */
    public Map<String, Action> getAction() {
        return actions;
    }

    public void addAction(Action action) {
        actions.put(action.getName(), action);
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return actions.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return actions.containsValue(value);
    }

    @Override
    public Action get(Object key) {
        return actions.get(key);
    }

    @Override
    public Action put(String key, Action value) {
        assert false;
        return null;
    }

    @Override
    public Action remove(Object key) {
        assert false;
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Action> m) {
        assert false;
    }

    @Override
    public void clear() {
        assert false;
    }

    @Override
    public Set<String> keySet() {
        return actions.keySet();
    }

    @Override
    public Collection<Action> values() {
        return actions.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Action>> entrySet() {
        return actions.entrySet();
    }

    @Override
    public Iterator<Action> iterator() {
        return actions.values().iterator();
    }
}
