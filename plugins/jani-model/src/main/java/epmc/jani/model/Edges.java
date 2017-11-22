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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * Represents the set of edges of an automaton.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Edges implements JANINode, List<Edge> {
    /** Maps names of valid variables to valid variables. */
    private Map<String, JANIIdentifier> validIdentifiers;
    /** Maps names of valid locations to valid locations. */
    private Map<String, Location> validLocations;

    /** List of edges. */
    private final List<Edge> edges = new ArrayList<>();
    private ModelJANI model;

    /**
     * Sets the map of actions of the automaton the edges are part of.
     * The parameter given is a map from variable names to variables.
     * This method must be called exactly once before parsing. It must not be
     * called with a {@code null} parameter or with a parameter containing
     * {@code null} entries.
     * 
     * @param validVariables variables of the model the edges description is part of
     */
    void setValidIdentifiers(Map<String,JANIIdentifier> validVariables) {
        assert this.validIdentifiers == null;
        assert validVariables != null;
        for (Entry<String, JANIIdentifier> entry : validVariables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.validIdentifiers = validVariables;
    }

    /**
     * Set locations of the automaton the edges belong to.
     * 
     * @param locations locations of the automaton the edges belong to
     */
    void setValidLocations(Map<String,Location> locations) {
        assert this.validLocations == null;
        assert locations != null;
        for (Entry<String, Location> entry : locations.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.validLocations = locations;
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
        assert validLocations != null;
        JsonArray array = UtilJSON.toArrayObject(value);
        for (JsonValue edgeValue : array) {
            Edge edge = new Edge();
            edge.setValidIdentifiers(validIdentifiers);
            edge.setValidLocations(validLocations);
            edge.setModel(model);
            edge.parse(edgeValue);
            edges.add(edge);
        }
        return this;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Edge edge : edges) {
            result.add(edge.generate());
        }
        return result.build();
    }

    @Override
    public Iterator<Edge> iterator() {
        return edges.iterator();
    }	

    @Override
    public int size() {
        return edges.size();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return edges.contains(o);
    }

    @Override
    public Object[] toArray() {
        return edges.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return edges.toArray(a);
    }

    @Override
    public boolean add(Edge e) {
        return edges.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return edges.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return edges.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        return edges.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Edge> c) {
        return edges.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return edges.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return edges.retainAll(c);
    }

    @Override
    public void clear() {
        edges.clear();
    }

    @Override
    public Edge get(int index) {
        return edges.get(index);
    }

    @Override
    public Edge set(int index, Edge element) {
        return edges.set(index, element);
    }

    @Override
    public void add(int index, Edge element) {
        edges.add(index, element);
    }

    @Override
    public Edge remove(int index) {
        return edges.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return edges.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return edges.lastIndexOf(o);
    }

    @Override
    public ListIterator<Edge> listIterator() {
        return edges.listIterator();
    }

    @Override
    public ListIterator<Edge> listIterator(int index) {
        return edges.listIterator(index);
    }

    @Override
    public List<Edge> subList(int fromIndex, int toIndex) {
        return edges.subList(fromIndex, toIndex);
    }
}
