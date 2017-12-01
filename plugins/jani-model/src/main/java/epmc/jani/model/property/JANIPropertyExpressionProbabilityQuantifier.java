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
public final class JANIPropertyExpressionProbabilityQuantifier implements JANIExpression {
    /** Identifier of this JANI expression type. */
    public final static String IDENTIFIER = "jani-property-expression-probability-quantifier";
    private final static String OP = "op";
    private final static String PMIN = "Pmin";
    private final static String PMAX = "Pmax";
    private final static String EXP = "exp";
    private final static Map<String,DirType> STRING_TO_DIR_TYPE;
    static {
        Map<String,DirType> stringToDirType = new LinkedHashMap<>();
        stringToDirType.put(PMIN, DirType.MIN);
        stringToDirType.put(PMAX, DirType.MAX);
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
    }

    public JANIPropertyExpressionProbabilityQuantifier() {
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
        Expression quantified = expressionQuantifier.getQuantified();
        if (quantified instanceof ExpressionReward || quantified instanceof ExpressionSteadyState) {
            return null;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        exp = parser.matchExpression(model, quantified);
        dirType = expressionQuantifier.getDirType();
        switch (dirType) {
        case MAX:
            opValue = PMAX;
            break;
        case MIN:
            opValue = PMIN;
            break;
        default:
            //			the only possibility is "NONE", i.e., we are in a dtmc/ctmc model
            //			thus, every operator is OK.
            opValue = PMAX;
            break;
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
        return new ExpressionQuantifier.Builder()
                .setCmpType(CmpType.IS)
                .setDirType(dirType)
                .setQuantified(exp.getExpression())
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
