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

import javax.json.JsonNumber;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.ValueInteger;

/**
 * JANI expression for an integer literal.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionInt implements JANIExpression {
	public final static String IDENTIFIER = "int";
	
	private boolean initialized = false;

	/** Model to which this JANI node belongs. */
	private ModelJANI model;

	/** Integer literal this expression stores. */
	private int number;
	
	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}

	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		return parseAsJANIExpression(value);
	}
	
	@Override 
	public JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		initialized = false;
		if (!(value instanceof JsonNumber)) {
			return null;
		}
		JsonNumber number = (JsonNumber) value;
		if (!number.isIntegral()) {
			return null;
		}
		this.number = number.intValue();
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		assert model != null;
		return UtilJSON.toIntegerValue(number);
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) {
		assert expression != null;
		assert model != null;
		initialized = false;
		if (!(expression instanceof ExpressionLiteral)) {
			return null;
		}
		ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
		if (!ValueInteger.isInteger(expressionLiteral.getValue())) {
			return null;
		}
		number = ValueInteger.asInteger(expressionLiteral.getValue()).getInt();
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() {
		assert initialized;
		assert model != null;
		TypeInteger typeInteger = TypeInteger.get();
		return new ExpressionLiteral.Builder()
				.setValue(UtilValue.newValue(typeInteger, number))
				.build();
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
