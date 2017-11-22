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

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeReal implements JANIType {
    public final static String IDENTIFIER = "real";
    /** Identifier for real type. */
    private final static String REAL = "real";
    /** Whether the last try to parse type was successful. */
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
        if (!(value instanceof JsonString)) {
            return null;
        }
        JsonString valueString = (JsonString) value;
        if (!valueString.getString().equals(REAL)) {
            return null;
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        return UtilJSON.toStringValue(REAL);
    }

    @Override
    public TypeReal toType() {
        return TypeReal.get();
    }

    @Override
    public Value getDefaultValue() {
        return UtilValue.newValue(toType(), 0);
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
