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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.type.JANIType;
import epmc.util.UtilJSON;
import epmc.value.Type;

public final class Constants implements JANINode, Iterable<Constant>, ExpressionToType {
    /** Model which this constants belong to. */
    private ModelJANI model;
    /** Map from constant names to constants. */
    private final Map<String,Constant> constants = new LinkedHashMap<>();

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
        assert value != null;
        JsonArray array = UtilJSON.toArray(value);
        for (JsonValue var : array) {
            Constant constant = new Constant();
            constant.setModel(model);
            constant.setValidIdentifiers(constants);
            constant.parse(var);
            constants.put(constant.getName(), constant);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Constant constant : constants.values()) {
            builder.add(constant.generate());
        }
        return builder.build();
    }

    @Override
    public Iterator<Constant> iterator() {
        return constants.values().iterator();
    }

    public Map<String, Constant> getConstants() {
        return constants;
    }

    public void put(String name, Constant constant) {
        constants.put(name, constant);
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        ExpressionIdentifierStandard identifier = ExpressionIdentifierStandard.as(expression);
        if (identifier == null) {
            return null;
        }
        if (identifier.getScope() != null) {
            return null;
        }
        Constant constant = constants.get(identifier.getName());
        if (constant == null) {
            return null;
        }
        JANIType type = constant.getType();
        if (type == null) {
            return null;
        }
        return type.toType();
    }
}
