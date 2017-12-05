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

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

/**
 * JANI expression for a boolean literal.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionBool implements JANIExpression {
    public final static String IDENTIFIER = "bool";

    /** JANI model to which this expression belongs. */
    private ModelJANI model;

    private boolean initialized;
    /** Boolean value of expression. */
    private boolean value;
    /** Positional information. */
    private Positional positional;
    
    private void resetFields() {
        initialized = false;
    }

    public JANIExpressionBool() {
        resetFields();
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
        return parseAsJANIExpression(value);
    }

    @Override 
    public JANIExpression parseAsJANIExpression(JsonValue value) {
        assert model != null;
        assert value != null;
        resetFields();
        if (value.getValueType() != ValueType.TRUE
                && value.getValueType() != ValueType.FALSE) {
            return null;
        }
        this.value = value.getValueType() == ValueType.TRUE;
        initialized = true;
        positional = UtilModelParser.getPositional(value);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
        return UtilJSON.toBooleanValue(value);
    }

    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        resetFields();
        if (!ExpressionLiteral.is(expression)) {
            return null;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        if (!expressionLiteral.getType().equals(ExpressionTypeBoolean.TYPE_BOOLEAN)) {
            return null;
        }
        value = Boolean.valueOf(expressionLiteral.getValue());
        initialized = true;
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        return new ExpressionLiteral.Builder()
                .setValue(value ? "true" : "false")
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .setPositional(positional)
                .build();
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
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
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
