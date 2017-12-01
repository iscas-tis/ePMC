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

import java.util.Map;

import javax.json.Json;
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
 * JANI expression for binary operators.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionOperatorBinary implements JANIExpression {
    public final static String IDENTIFIER = "operator-binary";
    private final static String OP = "op";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;
    private boolean forProperty;

    private boolean initialized;
    private JANIOperator operator;
    private JANIExpression left;
    private JANIExpression right;
    /** Positional information. */
    private Positional positional;
    
    private void resetFields() {
        initialized = false;
        operator = null;
        left = null;
        right = null;
        positional = null;
    }

    public JANIExpressionOperatorBinary() {
        resetFields();
    }

    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIExpression(value);
    }

    @Override 
    public JANIExpression parseAsJANIExpression(JsonValue value) {
        assert model != null;
        assert validIdentifiers != null;
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
        if (operator.getArity() != 2) {
            return null;
        }
        if (!object.containsKey(LEFT)) {
            return null;
        }
        if (!object.containsKey(RIGHT)) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        left = parser.parseAsJANIExpression(object.get(LEFT));
        if (left == null) {
            return null;
        }
        right = parser.parseAsJANIExpression(object.get(RIGHT));
        if (right == null) {
            return null;
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
        builder.add(LEFT, left.generate());
        builder.add(RIGHT, right.generate());
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
        if (operator.getArity() != 2) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        left = parser.matchExpression(model, expressionOperator.getOperand1());
        if (left == null) {
            return null;
        }
        right = parser.matchExpression(model, expressionOperator.getOperand2());
        if (right == null) {
            return null;
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
        Expression result = new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(left.getExpression(), right.getExpression())
                .setPositional(positional)
                .build();
        return result;
    }

    private JANIOperators getJANIOperators() {
        assert model != null;
        return model.getJANIOperators();
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
    public void setPositional(Positional positional) {
        this.positional = positional;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
    
    @Override
    public void setForProperty(boolean forProperty) {
        this.forProperty = forProperty;
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }	

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
