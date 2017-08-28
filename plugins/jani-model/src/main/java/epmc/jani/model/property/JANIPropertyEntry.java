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

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

public final class JANIPropertyEntry implements JANINode {
    /** String identifying name of a property. */
    private final static String NAME = "name";
    /** String identifying expression (definition) of a property. */
    private final static String EXPRESSION = "expression";
    /** String identifying optional comment of a property. */
    private final static String COMMENT = "comment";

    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private String name;
    private Expression expression;
    private String comment;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setValidIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert value != null;
        assert model != null;
        assert validIdentifiers != null;
        JsonObject object = UtilJSON.toObject(value);
        name = UtilJSON.getString(object, NAME);
        JsonValue expressionValue = UtilJSON.get(object, EXPRESSION);
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, true);
        JANIExpression parsed = parser.parseAsJANIExpression(expressionValue);
        if (parsed != null) {
            expression = parsed.getExpression();
        }
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public JsonValue generate() {
        assert model != null;
        assert validIdentifiers != null;
        assert name != null;
        assert expression != null;
        JsonObjectBuilder property = Json.createObjectBuilder();
        property.add(NAME, name);
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, true);
        JANIExpression propertyExp = parser.matchExpression(model, expression);
        property.add(EXPRESSION, propertyExp.generate());
        if (comment != null) {
            property.add(COMMENT, comment);
        }
        return property.build();
    }

}
