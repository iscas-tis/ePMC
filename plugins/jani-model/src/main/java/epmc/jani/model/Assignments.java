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
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * Stores the assignments of a destination.
 * After parsing, objects of this class act as a map from variables to
 * expressions of the values being assigned to the according variable.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Assignments implements JANINode, Set<AssignmentSimple> {
    /** Map mapping Strings to valid variables. */
    private Map<String,JANIIdentifier> validIdentifiers;	
    /** Map from variables to expression about assignment to variable. */
    private final Set<AssignmentSimple> assignments = new LinkedHashSet<>();
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
        for (JsonValue assignmentValue : array) {
            assignments.add(UtilModelParser.parse(model, () -> {
                AssignmentSimple assignment = new AssignmentSimple();
                assignment.setModel(model);
                assignment.setValidIdentifiers(validIdentifiers);
                return assignment;
            }, assignmentValue));
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (AssignmentSimple assignment : assignments) {
            result.add(assignment.generate());
        }
        return result.build();
    }

    @Override
    public int size() {
        return assignments.size();
    }

    @Override
    public boolean isEmpty() {
        return assignments.isEmpty();
    }

    @Override
    public void clear() {
        assert false;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void addAssignment(AssignmentSimple assignment) {
        assignments.add(assignment);
    }

    @Override
    public boolean contains(Object o) {
        return assignments.contains(o);
    }

    @Override
    public Iterator<AssignmentSimple> iterator() {
        return assignments.iterator();
    }

    @Override
    public Object[] toArray() {
        return assignments.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return assignments.toArray(a);
    }

    @Override
    public boolean add(AssignmentSimple e) {
        return assignments.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return assignments.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return assignments.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends AssignmentSimple> c) {
        return assignments.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return assignments.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return assignments.removeAll(c);
    }
}
