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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * Represents the list of transient values of a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TransientValues implements JANINode, Set<TransientValue> {
    /** Map mapping Strings to valid variables. */
    private Map<String,JANIIdentifier> validIdentifiers;	
    /** Set of transient values. */
    private final Set<TransientValue> transientValues = new LinkedHashSet<>();
    /** Model to which these assignments belong. */
    private ModelJANI model;

    /**
     * Set valid variable assignments.
     * This method must be called exactly once before parsing. It must not be
     * called with a {@code null} parameter or with a parameter containing {@code
     * null} entries.
     * 
     * @param variables variables which can be assigned
     */
    void setValidIdentifiers(Map<String,JANIIdentifier> variables) {
        assert this.validIdentifiers == null;
        assert variables != null;
        for (Entry<String, JANIIdentifier> entry : variables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.validIdentifiers = variables;
    }

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
        assert validIdentifiers != null;
        JsonArray array = UtilJSON.toArrayObject(value);
        for (JsonValue transientValue : array) {
            transientValues.add(UtilModelParser.parse(model, () -> {
                TransientValue trv = new TransientValue();
                trv.setModel(model);
                trv.setValidIdentifiers(validIdentifiers);
                return trv;
            }, transientValue));
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (TransientValue transientValue : transientValues) {
            result.add(transientValue.generate());
        }
        return result.build();
    }

    @Override
    public int size() {
        return transientValues.size();
    }

    @Override
    public boolean isEmpty() {
        return transientValues.isEmpty();
    }

    @Override
    public void clear() {
        assert false;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void addAssignment(TransientValue assignment) {
        transientValues.add(assignment);
    }

    @Override
    public boolean contains(Object o) {
        return transientValues.contains(o);
    }

    @Override
    public Iterator<TransientValue> iterator() {
        return transientValues.iterator();
    }

    @Override
    public Object[] toArray() {
        return transientValues.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return transientValues.toArray(a);
    }

    @Override
    public boolean add(TransientValue e) {
        return transientValues.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return transientValues.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return transientValues.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TransientValue> c) {
        return transientValues.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return transientValues.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return transientValues.removeAll(c);
    }
}
