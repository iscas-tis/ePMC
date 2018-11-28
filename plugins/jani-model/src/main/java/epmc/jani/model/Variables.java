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

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.type.JANIType;
import epmc.util.UtilJSON;
import epmc.value.Type;

/**
 * Represents the global variables of a model or the variables of an automaton.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Variables implements JANINode, Iterable<Variable>, Map<String,Variable>, ExpressionToType {
    /** Automaton which this variables list is part of, or {@code null}. */
    private Automaton automaton;

    /** Map from variable names to variables. */
    private final Map<String,Variable> variables = new LinkedHashMap<>();;
    /** Model to which these variables belong to. */
    private ModelJANI model;

    private Map<String, ? extends JANIIdentifier> identifiers;

    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    void setAutomaton(Automaton automaton) {
        assert this.automaton == null;
        assert automaton != null;
        this.automaton = automaton;
    }

    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
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
        assert value != null;
        JsonArray array = UtilJSON.toArrayObject(value);
        // TODO check duplicates?
        for (JsonValue var : array) {
            Variable variable = new Variable();
            variable.setAutomaton(automaton);
            variable.setModel(model);
            variable.parse(var);
            variable.setIdentifiers(identifiers);
            variables.put(variable.getName(), variable);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Variable variable : variables.values()) {
            result.add(variable.generate());
        }
        return result.build();
    }

    /**
     * Obtain a map from variable names to variables.
     * This method must not be called before parsing. The result of this method
     * is unmodifiable.
     * 
     * @return map from variable names to variables
     */
    public Map<String, Variable> getVariables() {
        return variables;
    }

    @Override
    public Iterator<Variable> iterator() {
        return variables.values().iterator();
    }

    @Override
    public int size() {
        return variables.size();
    }

    @Override
    public boolean isEmpty() {
        return variables.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        assert key != null;
        return variables.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        assert value != null;
        return variables.containsKey(value);
    }

    @Override
    public Variable get(Object key) {
        assert key != null;
        return variables.get(key);
    }

    public Variable get(int variableNr) {
        assert variableNr >= 0;
        assert variableNr < variables.size();
        int vNr = 0;
        for (Variable variable : variables.values()) {
            if (vNr == variableNr) {
                return variable;
            }
            vNr++;
        }
        assert false;
        return null;
    }

    @Override
    public Variable put(String key, Variable value) {
        assert false;
        return null;
    }

    @Override
    public Variable remove(Object key) {
        assert false;
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Variable> m) {
        assert false;
    }

    @Override
    public void clear() {
        assert false;
    }

    @Override
    public Set<String> keySet() {
        return variables.keySet();
    }

    @Override
    public Collection<Variable> values() {
        return variables.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Variable>> entrySet() {
        return variables.entrySet();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public Map<String,Expression> getIdentifiers() {
        EntryTransformer<String, Variable, Expression> flagPrefixer =
                new EntryTransformer<String, Variable, Expression>() {
            @Override
            public Expression transformEntry(String key, Variable value) {
                return value.getIdentifier();
            }
        };
        return Maps.transformEntries(variables, flagPrefixer);
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        ExpressionIdentifierStandard identifier = ExpressionIdentifierStandard.as(expression);
        if (identifier == null) {
            return null;
        }
        if (identifier.getScope() != automaton) {
            return null;
        }
        Variable variable = variables.get(identifier.getName());
        if (variable == null) {
            return null;
        }
        JANIType type = variable.getType();
        if (type == null) {
            return null;
        }
        return type.toType();
    }
}
