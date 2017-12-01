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
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.operator.OperatorIte;
import epmc.util.UtilJSON;

/**
 * JANI expression for the if-then-else (ITE) operator
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionOperatorIfThenElse implements JANIExpression {
    public final static String IDENTIFIER = "operator-if-then-else";
    private final static String OP = "op";
    private final static String ITE = "ite";
    private final static String IF = "if";
    private final static String THEN = "then";
    private final static String ELSE = "else";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private boolean forProperty;
    private ModelJANI model;

    private boolean initialized;
    private JANIExpression ifExpr;
    private JANIExpression thenExpr;
    private JANIExpression elseExpr;
    /** Positional information. */
    private Positional positional;

    private void resetFields() {
        initialized = false;
        ifExpr = null;
        thenExpr = null;
        elseExpr = null;
        positional = null;
    }

    public JANIExpressionOperatorIfThenElse() {
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
        String op = UtilJSON.getString(object, OP);
        if (!op.equals(ITE)) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        ifExpr = parser.parseAsJANIExpression(UtilJSON.get(object, IF));
        if (ifExpr == null) {
            return null;
        }
        thenExpr = parser.parseAsJANIExpression(UtilJSON.get(object, THEN));
        if (thenExpr == null) {
            return null;
        }
        elseExpr = parser.parseAsJANIExpression(UtilJSON.get(object, ELSE));
        if (elseExpr == null) {
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
        builder.add(OP, ITE);
        builder.add(IF, ifExpr.generate());
        builder.add(THEN, thenExpr.generate());
        builder.add(ELSE, elseExpr.generate());
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
        if (!expressionOperator.getOperator().equals(OperatorIte.ITE)) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        ifExpr = parser.matchExpression(model, expressionOperator.getOperand1());
        if (ifExpr == null) {
            return null;
        }
        thenExpr = parser.matchExpression(model, expressionOperator.getOperand2());
        if (thenExpr == null) {
            return null;
        }
        elseExpr = parser.matchExpression(model, expressionOperator.getOperand3());
        if (elseExpr == null) {
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
        return new ExpressionOperator.Builder()
                .setOperator(OperatorIte.ITE)
                .setOperands(ifExpr.getExpression(),
                        thenExpr.getExpression(),
                        elseExpr.getExpression())
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
