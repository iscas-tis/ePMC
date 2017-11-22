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

package epmc.jani.model;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.model.expression.ExpressionParser;
import epmc.util.UtilJSON;

public final class InitialStates implements JANINode {
    private final static String EXP = "exp";
    private final static String COMMENT = "comment";
    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> identifiers;
    private Expression exp;
    private String comment;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setIdentifier(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert value != null;
        JsonObject object = UtilJSON.toObject(value);
        exp = ExpressionParser.parseExpression(model, object.get(EXP), identifiers);
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(EXP, ExpressionParser.generateExpression(model, exp));
        UtilJSON.addOptional(builder, COMMENT, comment);
        return builder.build();
    }

    public void setExp(Expression exp) {
        this.exp = exp;
    }

    public Expression getExp() {
        return exp;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

}
