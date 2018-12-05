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

package epmc.jani.extensions.functions;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

public final class JANIExpressionOperatorCall implements JANIExpression {
    public final static String IDENTIFIER = "operator-call";
    private final static String OP = "op";
    private final static String ARGS = "args";
    private final static String CALL = "call";
    private final static String FUNCTION = "function";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;
    private boolean forProperty;

    private boolean initialized;
    private JANIExpression[] operands;
    private String function;

    /** Positional information. */
    private Positional positional;

    private void resetFields() {
        initialized = false;
        operands = null;
        positional = null;
        function = null;
    }

    public JANIExpressionOperatorCall() {
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
        if (!UtilJSON.getStringOrEmpty(object, OP).equals(CALL)) {
            return null;
        }
        if (!object.containsKey(ARGS)) {
            return null;
        }
        function = UtilJSON.getString(object, FUNCTION);
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
        builder.add(OP, CALL);
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (int i = 0; i < this.operands.length; i++) {
            array.add(operands[i].generate());
        }
        builder.add(ARGS, array);
        builder.add(FUNCTION, function);
        UtilModelParser.addPositional(builder, positional);
        return builder.build();
    }


    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        assert model != null;
        assert validIdentifiers != null;
        resetFields();
        if (!(expression instanceof ExpressionCall)) {
            return null;
        }
        ExpressionCall expressionOperator = (ExpressionCall) expression;
        List<Expression> operands = expressionOperator.getOperands();
        this.operands = new JANIExpression[operands.size()];
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        for (int i = 0; i < operands.size(); i++) {
            this.operands[i] = parser.matchExpression(model, operands.get(i));
            if (this.operands[i] == null) {
                return null;
            }
        }
        function = expressionOperator.getFunction();
        initialized = true;
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        Expression[] operands = new Expression[this.operands.length];
        for (int i = 0; i < this.operands.length; i++) {
            operands[i] = this.operands[i].getExpression();
        }
        return new ExpressionCall.Builder()
                .setOperands(operands)
                .setFunction(function)
                .setPositional(positional)
                .build();
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
