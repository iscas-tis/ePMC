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

package epmc.jani.model.property;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

/**
 * JANI quantifier expression.
 * 
 * @author Ernst Moritz Hahn
 * @author Andrea Turrini
 */
public final class JANIPropertyExpressionSteadyStateQuantifier implements JANIExpression {
    /** Identifier of this JANI expression type. */
    public final static String IDENTIFIER = "jani-property-expression-steady-state-quantifier";
    private final static String OP = "op";
    private final static String SMIN = "Smin";
    private final static String SMAX = "Smax";
    private final static String EXP = "exp";
    private final static Map<String,DirType> STRING_TO_DIR_TYPE;
    static {
        Map<String,DirType> stringToDirType = new LinkedHashMap<>();
        stringToDirType.put(SMIN, DirType.MIN);
        stringToDirType.put(SMAX, DirType.MAX);
        STRING_TO_DIR_TYPE = Collections.unmodifiableMap(stringToDirType);
    }

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;
    private boolean forProperty;

    private boolean initialized;
    private String opValue;
    private DirType dirType;
    private JANIExpression exp;
    /** Positional information. */
    private Positional positional;


    private void resetFields() {
        initialized = false;
        opValue = null;
        dirType = null;
        exp = null;
        positional = null;
    }

    public JANIPropertyExpressionSteadyStateQuantifier() {
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
        if (!forProperty) {
            return null;
        }
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
        if (!object.containsKey(EXP)) {
            return null;
        }
        dirType = UtilJSON.toOneOfOrNullFailInvalidType(object, OP, STRING_TO_DIR_TYPE);
        if (dirType == null) {
            return null;
        }
        opValue = UtilJSON.getString(object, OP);
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        exp = parser.parseAsJANIExpression(object.get(EXP));
        if (exp == null) {
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
        builder.add(OP, opValue);
        builder.add(EXP, exp.generate());
        UtilModelParser.addPositional(builder, positional);
        return builder.build();
    }

    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        assert model != null;
        assert validIdentifiers != null;
        resetFields();
        if (!(expression instanceof ExpressionQuantifier)) {
            return null;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        dirType = expressionQuantifier.getDirType();
        switch (dirType) {
        case MAX:
            opValue = SMAX;
            break;
        case MIN:
            opValue = SMIN;
            break;
        default:
            //			the only possibility is "NONE", i.e., we are in a dtmc/ctmc model
            //			thus, every operator is OK.
            opValue = SMAX;
            break;
        }
        Expression quantified = expressionQuantifier.getQuantified();
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        if (!(quantified instanceof ExpressionSteadyState)) {
            //a property like R=?[S] has to be translated as S=?[reward_expression]...
            if (quantified instanceof ExpressionReward
                    && ((ExpressionReward) quantified).getRewardType().isSteadystate()) {
                exp = parser.matchExpression(model, ((ExpressionReward) quantified).getReward().getExpression());
                initialized = true;
            } else {
                return null;				
            }
        } else {
            ExpressionSteadyState expressionSteadyState = (ExpressionSteadyState) quantified;
            exp = parser.matchExpression(model, expressionSteadyState.getOperand1());
            initialized = true;
        }
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        return new ExpressionQuantifier.Builder()
                .setDirType(dirType)
                .setCmpType(CmpType.IS)
                .setQuantified(new ExpressionSteadyState.Builder()
                        .setStates(exp.getExpression())
                        .build())
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
