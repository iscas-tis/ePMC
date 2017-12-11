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

package epmc.jani.model.type;

import java.util.Collections;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.jani.model.Constant;
import epmc.jani.model.Constants;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class JANITypeBounded implements JANIType {
    public final static String IDENTIFIER = "bounded";
    /** Identifies the type of variable type specification. */
    private final static String KIND = "kind";
    /** Identifier for bounded type. */
    private final static String BOUNDED = "bounded";
    /** Identifier for base type of bounded type. */
    private final static String BASE = "base";
    /** Identifier for integer base type of bounded type. */
    private final static String INT = "int";
    /** Identifier for real base type of bounded type. */
    private final static String REAL = "real";
    /** Identifier of lower bound of bounded type. */
    private final static String LOWER_BOUND = "lower-bound";
    /** Identifier for upper bond of bounded type. */
    private final static String UPPER_BOUND = "upper-bound";

    private Expression lowerBound;
    private Expression upperBound;
    private ModelJANI model;

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
        return parseAsJANIType(value);
    }

    @Override 
    public JANIType parseAsJANIType(JsonValue value) {
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (!object.containsKey(KIND)) {
            return null;
        }
        if (!(object.get(KIND) instanceof JsonString)) {
            return null;
        }
        if (!object.getString(KIND).equals(BOUNDED)) {
            return null;
        }
        if (!object.containsKey(BASE)) {
            return null;
        }
        if (!(object.get(BASE) instanceof JsonString)) {
            return null;
        }
        if (!(object.getString(BASE).equals(INT) || object.getString(BASE).equals(REAL))) {
            return null;
        }
        Constants modelConstants = model.getModelConstants();
        Map<String, Constant> constants;
        if (modelConstants == null) {
            constants = Collections.emptyMap();
        } else {
            constants = modelConstants.getConstants();
        }
        if (object.containsKey(LOWER_BOUND)) {
            lowerBound = ExpressionParser.parseExpression(model, object.get(LOWER_BOUND), constants);
        } else {
            lowerBound = null;
        }
        if (object.containsKey(UPPER_BOUND)) {
            upperBound = ExpressionParser.parseExpression(model, object.get(UPPER_BOUND), constants);
        } else {
            upperBound = null;
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder().add(KIND, BOUNDED);

        result.add(BASE, INT);
        if (lowerBound != null) {
            result.add(LOWER_BOUND, ExpressionParser.generateExpression(model, lowerBound));
        }
        if (upperBound != null) {
            result.add(UPPER_BOUND, ExpressionParser.generateExpression(model, upperBound));
        }
        return result.build();
    }

    @Override
    public TypeInteger toType() {
        int lowerInt = Integer.MIN_VALUE;
        int upperInt = Integer.MAX_VALUE;
        if (lowerBound != null) {
            if (model != null) {
                lowerBound = UtilExpressionStandard.replace(lowerBound, model.getConstants());
            }
            // TODO HACK
            try {
                Value lowerValue = UtilEvaluatorExplicit.evaluate(lowerBound);
                if (ValueInteger.is(lowerValue)) {
                    lowerInt = ValueInteger.as(lowerValue).getInt();
                }
            } catch (Throwable e) {
                
            }
        }
        if (upperBound != null) {
            if (model != null) {
                upperBound = UtilExpressionStandard.replace(upperBound, model.getConstants());
            }
            try {
                Value upperValue = UtilEvaluatorExplicit.evaluate(upperBound);
                if (ValueInteger.is(upperValue)) {
                    upperInt = ValueInteger.as(upperValue).getInt();
                }
            } catch (Throwable e) {
                
            }
        }
        return TypeInteger.get(lowerInt, upperInt);
    }

    public void setLowerBound(Expression lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(Expression upperBound) {
        this.upperBound = upperBound;
    }

    public Expression getLowerBound() {
        return lowerBound;
    }

    public Expression getUpperBound() {
        return upperBound;
    }

    @Override
    public JANIType replace(Map<Expression, Expression> map) {
        assert map != null;
        //		assert contextValue != null;
        Expression newLower = null;
        if (lowerBound != null) {
            newLower = UtilExpressionStandard.replace(lowerBound, map);
        }
        Expression newUpper = null;
        if (upperBound != null) {
            newUpper = UtilExpressionStandard.replace(upperBound, map);
        }
        JANITypeBounded result = new JANITypeBounded();
        result.setLowerBound(newLower);
        result.setUpperBound(newUpper);
        return result;
    }

    @Override
    public Value getDefaultValue() {
        TypeInteger type = toType();
        if (TypeInteger.as(type).getLowerInt() <= 0
                && 0 <= TypeInteger.as(type).getUpperInt()) {
            return UtilValue.newValue(type, 0);
        } else {
            return UtilValue.newValue(type, TypeInteger.as(type).getLowerInt());
        }
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
