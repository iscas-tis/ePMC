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

package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueDouble;


public final class ResultFormatterDouble implements ResultFormatter {
    public final static String IDENTIFIER = "double";
    private final static String DECIMAL = "decimal";
    private final static JsonValue DECIMAL_VALUE = UtilJSON.toStringValue(DECIMAL);
    private Object result;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setResult(Object result) {
        assert result != null;
        this.result = result;
    }

    @Override
    public boolean canHandle() {
        if (!(result instanceof Value)) {
            return false;
        }
        Value valueResult = (Value) result;
        if (!ValueDouble.is(valueResult)) {
            return false;
        }
        return true;
    }

    @Override
    public String getLabel() {
        assert canHandle();
        return null;
    }

    @Override
    public JsonValue getType() {
        assert canHandle();
        return DECIMAL_VALUE;
    }

    @Override
    public JsonValue getValue() {
        assert canHandle();
        ValueDouble valueResult = ValueDouble.as((Value) result);
        return UtilJSON.toNumberValue(valueResult.getDouble());
    }

    @Override
    public String getFormattedValue() {
        assert canHandle();
        return result.toString();
    }

}
