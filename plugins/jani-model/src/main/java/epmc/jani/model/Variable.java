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

import java.util.Collections;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.TypeParser;
import epmc.jani.valuejson.UtilValueJSON;
import epmc.util.UtilJSON;
import epmc.value.Type;

/**
 * JANI variable specification.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Variable implements JANINode, JANIIdentifier {
    /** Identifies a variable. */
    private final static String TYPE = "type";
    /** Identifies the name of a variable. */
    private final static String NAME = "name";
    /** Identifies whether the variable is transient. */
    private final static String TRANSIENT = "transient";
    /** Identifier for initial value of the variable. */
    private final static String INITIAL_VALUE = "initial-value";
    /** String identifying comment of this variable. */
    private final static String COMMENT = "comment";

    /** Name of the variable. */
    private String name;
    /**
     * Identifier representing the variable.
     * If the variable belongs to a certain automaton, the context of the
     * identifier will be this automaton.
     * */
    private ExpressionIdentifierStandard identifier;
    /** Type of the variable. */
    private JANIType type;
    /** Automaton to which this variable belongs, or {@code null}. */
    private Automaton automaton;
    /** JANI model to which this node belongs. */
    private ModelJANI model;
    /** The initial value of the variable, or {@code null} to denote that the initial value
     * has been omitted.
     * If {@link #initialValue} is {@code null}, then the default value is the default value 
     * of the according type obtained by {@link JANIType#getDefaultValue()}. 
     * */
    private JANIExpression initialValue;
    private boolean transientVarSet;
    private boolean transientVar;
    /** Comment for this automaton. */
    private String comment;
    private Map<String, ? extends JANIIdentifier> identifiers;

    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public void setAutomaton(Automaton automaton) {
        assert this.automaton == null;
        this.automaton = automaton;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdentifier(ExpressionIdentifierStandard identifier) {
        this.identifier = identifier;
    }

    public void setType(JANIType type) {
        this.type = type;
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
        JsonObject object = UtilJSON.toObject(value);
        name = UtilJSON.getIdentifier(object, NAME);
        identifier = new ExpressionIdentifierStandard.Builder()
                .setName(name)
                .setScope(automaton)
                .build();
        JsonValue typeV = object.get(TYPE);
        TypeParser typeParser = new TypeParser();
        typeParser.setModel(model);
        typeParser.parse(typeV);
        type = typeParser.getType();
        type.setModel(model);
        type.parse(typeV);
        if (object.containsKey(INITIAL_VALUE)) {
            ExpressionParser parser = new ExpressionParser(model, Collections.emptyMap(), false);
            parser.setIdentifiers(identifiers);
            initialValue = parser.parseAsJANIExpression(object.get(INITIAL_VALUE));
        } else {
            initialValue = null;
        }
        if (object.containsKey(TRANSIENT)) {
            transientVarSet = true;
            transientVar = UtilJSON.getBoolean(object, TRANSIENT);
        } else {
            transientVarSet = false;
            transientVar = false;
        }

        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(NAME, name);
        assert type != null;
        result.add(TYPE, type.generate());
        if (initialValue != null) {
            result.add(INITIAL_VALUE, initialValue.generate());
        } else {
            if (transientVarSet && transientVar) {
                result.add(INITIAL_VALUE, UtilValueJSON.valueToJson(type.getDefaultValue()));
            }
        }
        if (transientVarSet) {
            result.add(TRANSIENT, transientVar);
        }
        UtilJSON.addOptional(result, COMMENT, comment);
        return result.build();
    }

    /**
     * Get name of variable.
     * 
     * @return name of variable
     */
    @Override
    public String getName() {
        return name;
    }

    // TODO documentation
    @Override
    public ExpressionIdentifierStandard getIdentifier() {
        return identifier;
    }

    /**
     * Get type of variable.
     * 
     * @return type of variable
     */
    @Override
    public JANIType getType() {
        return type;
    }	

    @Override
    public Variable clone() {
        Variable result = new Variable();
        result.name = this.name;
        result.type = this.type;
        result.identifier = identifier;
        return result;
    }

    /**
     * Convert JANI type of variable to EPMC {@link Type}.
     * 
     * @return EPMC type
     */
    public Type toType() {
        return type.toType();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void setInitialValueUndefined() {
        this.initialValue = null;
    }

    public void setInitial(JANIExpression expression) {
        assert expression != null;
        initialValue = expression;
    }

    public void setInitial(Expression expression) {
        if (expression == null) {
            initialValue = null;
        } else {
            ExpressionParser parser = new ExpressionParser(model, Collections.emptyMap(), false);
            initialValue = parser.matchExpression(model, expression);
        }
    }

    public boolean isInitialValueDefined() {
        return initialValue != null;
    }

    public JANIExpression getInitialValue() {
        return initialValue;
    }

    public Expression getInitialValueOrNull() {
        if (initialValue == null) {
            return null;
        } else {
            return initialValue.getExpression();
        }
    }

    public void setTransient(boolean transientVar) {
        this.transientVar = transientVar;
        transientVarSet = true;
    }

    public void setTransientUnset() {
        transientVar = false;
        transientVarSet = false;
    }

    public boolean isTransient() {
        return transientVar;
    }
    
    public boolean isTransientAssigned() {
        return transientVarSet;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

}
