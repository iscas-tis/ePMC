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

package epmc.jani.model.expression;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperator;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.operator.Operator;
import epmc.util.UtilJSON;

/**
 * JANI expression for generic operators.
 * This expression type can be used for operator expressions for which no
 * special form has yet been defined, such as those from inofficial model
 * extensions. As this generates nonstandard JANI code, this class should only
 * be used internally.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionOperatorGeneric implements JANIExpression {
    public final static String IDENTIFIER = "operator-generic";
    private final static String OP = "op";
    private final static String ARGS = "args";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;
    private boolean forProperty;

    private boolean initialized;
    private JANIOperator operator;
    private JANIExpression[] operands;

    /** Positional information. */
    private Positional positional;

    private void resetFields() {
        initialized = false;
        operator = null;
        operands = null;
        positional = null;
    }

    public JANIExpressionOperatorGeneric() {
        resetFields();
    }

    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIExpression(value);
    }

    @Override 
    public JANIExpression parseAsJANIExpression(JsonValue value) {
        assert model != null;
        assert value != null;
        resetFields();
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (!object.containsKey(OP)) {
            return null;
        }
        if (!(object.get(OP) instanceof JsonString)) {
            return null;
        }
        JANIOperators operators = model.getJANIOperators();
        if (!operators.containsOperatorByJANI(object.getString(OP))) {
            return null;
        }
        operator = UtilJSON.toOneOf(object, OP, operators::getOperatorByJANI);
        if (!object.containsKey(ARGS)) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        JsonArray operands = UtilJSON.getArray(object, ARGS);
        this.operands = new JANIExpression[operands.size()];
        for (int i = 0; i < operands.size(); i++) {
            this.operands[i] = parser.parseAsJANIExpression(operands.get(i));
            if (this.operands[i] == null) {
                return null;
            }
        }
        initialized = true;
        positional = UtilModelParser.getPositional(value);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(OP, operator.getJANI());
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (int i = 0; i < this.operands.length; i++) {
            assert operands[i] != null : operator;
            array.add(operands[i].generate());
        }
        builder.add(ARGS, array);
        UtilModelParser.addPositional(builder, positional);
        return builder.build();
    }


    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        assert model != null;
        assert validIdentifiers != null;
        resetFields();
        if (!(expression instanceof ExpressionOperator)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        operator = getJANIOperators().getOperator(expressionOperator.getOperator());
        if (operator == null) {
            return null;
        }
        List<Expression> operands = expressionOperator.getOperands();
        this.operands = new JANIExpression[operands.size()];
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        for (int i = 0; i < operands.size(); i++) {
            this.operands[i] = parser.matchExpression(model, operands.get(i));
            if (this.operands[i] == null) {
                return null;
            }
        }
        initialized = true;
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        Operator operator = this.operator.getOperator();
        Expression[] operands = new Expression[this.operands.length];
        for (int i = 0; i < this.operands.length; i++) {
            operands[i] = this.operands[i].getExpression();
        }
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operands)
                .setPositional(positional)
                .build();
    }

    private JANIOperators getJANIOperators() {
        assert model != null;
        return model.getJANIOperators();
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }	

    @Override
    public void setForProperty(boolean forProperty) {
        this.forProperty = forProperty;
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
    public String toString() {
        return UtilModelParser.toString(this);
    }
    
    @Override
    public void setPositional(Positional positional) {
        this.positional = positional;
    }
    
    @Override
    public Positional getPositional() {
        return positional;
    }
}
