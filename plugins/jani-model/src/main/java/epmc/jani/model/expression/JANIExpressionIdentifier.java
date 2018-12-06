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

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

/**
 * JANI expression representing an identifier in the model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionIdentifier implements JANIExpression {
    public final static String IDENTIFIER = "identifier";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;

    private boolean initialized;
    private ExpressionIdentifierStandard identifier;
    /** Positional information. */
    private Positional positional;

    private void resetFields() {
        initialized = false;
        identifier = null;
        positional = null;
    }

    public JANIExpressionIdentifier() {
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
//        assert validIdentifiers != null;
        assert value != null;
        resetFields();
        if (!(value instanceof JsonString)) {
            return null;
        }
        if (validIdentifiers != null) {
            JANIIdentifier janiIdentifier = UtilJSON.toOneOf(value, validIdentifiers);
            identifier = janiIdentifier.getIdentifier();
            if (identifier == null) {
                System.out.println(value + " " + janiIdentifier);
                System.out.println(validIdentifiers);
            }
        } else {
            identifier = new ExpressionIdentifierStandard.Builder()
                    .setName(value.toString())
                    .build();
        }
        initialized = true;
        positional = UtilModelParser.getPositional(value);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
//        assert validIdentifiers != null;
        return UtilJSON.toStringValue(identifier.getName());
    }

    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        assert model != null;
        assert validIdentifiers != null;
        resetFields();
        if (!(expression instanceof ExpressionIdentifier)) {
            return null;
        }
        this.identifier = (ExpressionIdentifierStandard) expression;
        initialized = true;
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        assert initialized;
        assert model != null;
//        assert validIdentifiers != null;
        return identifier.replacePositional(positional);
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
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
