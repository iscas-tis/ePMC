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
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.JANIExpression;

/**
 * JANI expression for initial state operators.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIPropertyInitial implements JANIExpression {
	/** Identifier of this JANI expression type. */
	public final static String IDENTIFIER = "initial";
	private final static String OP = "op";
	private final static String INITIAL = "initial";
	
	private ModelJANI model;
	private boolean initialized;

	private void resetFields() {
		initialized = false;
	}
	
	public JANIPropertyInitial() {
		resetFields();
	}

	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		return parseAsJANIExpression(value);
	}
	
	@Override 
	public JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException {
		assert model != null;
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
		if (!object.getString(OP).equals(INITIAL)) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert initialized;
		assert model != null;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(OP, INITIAL);
		return builder.build();
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) throws EPMCException {
		assert expression != null;
		assert model != null;
		resetFields();
		if (!(expression instanceof ExpressionInitial)) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		assert model != null;
		return new ExpressionInitial(null);
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
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
}
