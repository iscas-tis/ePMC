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

package epmc.time;

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.type.JANIType;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.UtilValue;
import epmc.value.Value;

/**
 * Clock type.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANITypeClock implements JANIType {
	public final static String IDENTIFIER = "clock";
	/** Identifier for boolean type. */
	private final static String CLOCK = "clock";
	
	private boolean initialized = false;

	/** Value context used. */
	private ContextValue contextValue;
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
	public JANINode parse(JsonValue value) throws EPMCException {
		return parseAsJANIType(value);
	}
	
	@Override 
	public JANIType parseAsJANIType(JsonValue value) throws EPMCException {
		assert model != null;
		initialized = false;
		if (!(value instanceof JsonString)) {
			return null;
		}
		JsonString valueString = (JsonString) value;
		if (!valueString.getString().equals(CLOCK)) {
			return null;
		}
		contextValue = model.getContextValue();
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		return UtilJSON.toStringValue(CLOCK);
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		this.contextValue = contextValue;
	}

	@Override
	public TypeClock toType() {
		return getContextValue().makeUnique(new TypeClock(getContextValue()));
	}

	/**
	 * Get value context from expression context of this JANI type.
	 * 
	 * @return value context from expression context of this JANI type
	 */
	private ContextValue getContextValue() {
		return contextValue;
	}

	@Override
	public Value getDefaultValue() throws EPMCException {
		return UtilValue.newValue(toType(), 0);
	}
}
