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

package epmc.jani.type.smg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Actions;
import epmc.jani.model.Automata;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class PlayersJANI implements JANINode, List<PlayerJANI> {

    private boolean initialized = false;

    private ModelJANI model;
    private final List<PlayerJANI> players = new ArrayList<>();
    private Actions validActions;
    private Automata validAutomata;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setValidActions(Actions actions) {
        this.validActions = actions;
    }

    public void setValidAutomata(Automata automata) {
        this.validAutomata = automata;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert value != null;
        initialized = false;
        players.clear();
        JsonArray array = UtilJSON.toArrayObject(value);
        for (JsonValue playerValue: array) {
            PlayerJANI player = new PlayerJANI();
            player.setModel(model);
            player.setValidActions(validActions);
            player.setValidAutomata(validAutomata);
            player.parse(playerValue);
            players.add(player);
        }
        initialized = true;
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (PlayerJANI player : players) {
            result.add(player.generate());
        }
        return result.build();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public Iterator<PlayerJANI> iterator() {
        assert players != null;
        return players.iterator();
    }

    @Override
    public int size() {
        assert players != null;
        return players.size();
    }

    @Override
    public boolean isEmpty() {
        assert players != null;
        return players.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        assert players != null;
        return players.contains(o);
    }

    @Override
    public Object[] toArray() {
        assert players != null;
        return players.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        assert players != null;
        return players.toArray(a);
    }

    @Override
    public boolean add(PlayerJANI e) {
        assert players != null;
        return players.add(e);
    }

    @Override
    public boolean remove(Object o) {
        assert players != null;
        return players.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        assert players != null;
        return players.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends PlayerJANI> c) {
        assert players != null;
        return players.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends PlayerJANI> c) {
        assert players != null;
        return players.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        assert players != null;
        return players.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        assert players != null;
        return players.retainAll(c);
    }

    @Override
    public void clear() {
        assert players != null;
        players.clear();
    }

    @Override
    public PlayerJANI get(int index) {
        assert players != null;
        return players.get(index);
    }

    @Override
    public PlayerJANI set(int index, PlayerJANI element) {
        assert players != null;
        return players.set(index, element);
    }

    @Override
    public void add(int index, PlayerJANI element) {
        assert players != null;
        players.add(index, element);
    }

    @Override
    public PlayerJANI remove(int index) {
        assert players != null;
        return players.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        assert players != null;
        return players.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        assert players != null;
        return players.lastIndexOf(o);
    }

    @Override
    public ListIterator<PlayerJANI> listIterator() {
        assert players != null;
        return players.listIterator();
    }

    @Override
    public ListIterator<PlayerJANI> listIterator(int index) {
        assert players != null;
        return players.listIterator(index);
    }

    @Override
    public List<PlayerJANI> subList(int fromIndex, int toIndex) {
        assert players != null;
        return players.subList(fromIndex, toIndex);
    }
}
